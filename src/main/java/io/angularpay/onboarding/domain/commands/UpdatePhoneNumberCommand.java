package io.angularpay.onboarding.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.onboarding.adapters.outbound.MongoAdapter;
import io.angularpay.onboarding.configurations.AngularPayConfiguration;
import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.domain.Role;
import io.angularpay.onboarding.exceptions.ErrorObject;
import io.angularpay.onboarding.helpers.CommandHelper;
import io.angularpay.onboarding.models.CreateOtpResponseApiModel;
import io.angularpay.onboarding.models.UpdatePhoneNumberCommandRequest;
import io.angularpay.onboarding.ports.outbound.NotificationServicePort;
import io.angularpay.onboarding.ports.outbound.OtpServicePort;
import io.angularpay.onboarding.util.TTLProcessManager;
import io.angularpay.onboarding.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.angularpay.onboarding.helpers.CommandHelper.*;
import static io.angularpay.onboarding.helpers.Helper.maskPhone;

@Service
public class UpdatePhoneNumberCommand extends AbstractCommand<UpdatePhoneNumberCommandRequest, Void>
        implements SensitiveDataCommand<UpdatePhoneNumberCommandRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final OtpServicePort otpServiceAdapter;
    private final NotificationServicePort notificationServiceAdapter;
    private final AngularPayConfiguration configuration;
    private final TTLProcessManager ttlProcessManager;

    public UpdatePhoneNumberCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            OtpServicePort otpServiceAdapter,
            NotificationServicePort notificationServiceAdapter,
            AngularPayConfiguration configuration,
            TTLProcessManager ttlProcessManager) {
        super("UpdatePhoneNumberCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.otpServiceAdapter = otpServiceAdapter;
        this.notificationServiceAdapter = notificationServiceAdapter;
        this.configuration = configuration;
        this.ttlProcessManager = ttlProcessManager;
    }

    @Override
    protected String getResourceOwner(UpdatePhoneNumberCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected Void handle(UpdatePhoneNumberCommandRequest request) {
        notExistsRequestByPhoneOrThrow(this.mongoAdapter, request.getUpdatePhoneNumberApiModel().getPhone());

        OnboardingRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        found.setPhone(request.getUpdatePhoneNumberApiModel().getPhone());
        found.setSignupConfirmed(false);
        OnboardingRequest response = this.mongoAdapter.updateRequest(found);

        Map<String, String> headers = new HashMap<>();
        headers.put("x-angularpay-username", response.getEmail());
        headers.put("x-angularpay-device-id", response.getDevice().getDeviceId());
        headers.put("x-angularpay-correlation-id", request.getAuthenticatedUser().getCorrelationId());

        CreateOtpResponseApiModel otpResponse = createOtpOrThrow(this.otpServiceAdapter, response, headers);
        sendOtpNotificationOrThrow(this.notificationServiceAdapter, response, otpResponse.getCode(), headers);

        ScheduledFuture<?> process = Executors.newScheduledThreadPool(1).schedule(
                () -> {
                    this.mongoAdapter.findRequestByReference(response.getReference()).ifPresent(reFound -> {
                        if (!reFound.isSignupConfirmed()) {
                            this.mongoAdapter.deleteOnboardingRequest(reFound);
                        }
                    });
                    this.ttlProcessManager.invalidate(response.getReference());
                },
                configuration.getOnboardingTTLMinutes(),
                TimeUnit.MINUTES
        );
        this.ttlProcessManager.put(response.getReference(), process);

        return null;
    }

    @Override
    protected List<ErrorObject> validate(UpdatePhoneNumberCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public UpdatePhoneNumberCommandRequest mask(UpdatePhoneNumberCommandRequest raw) {
        try {
            JsonNode node = mapper.convertValue(raw, JsonNode.class);
            JsonNode updatePhoneNumberApiModel = node.get("updatePhoneNumberApiModel");
            ((ObjectNode) updatePhoneNumberApiModel).put("phone", maskPhone(raw.getUpdatePhoneNumberApiModel().getPhone()));
            return mapper.treeToValue(node, UpdatePhoneNumberCommandRequest.class);
        } catch (JsonProcessingException exception) {
            return raw;
        }
    }

}
