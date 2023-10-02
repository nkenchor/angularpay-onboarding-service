package io.angularpay.onboarding.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.onboarding.adapters.outbound.MongoAdapter;
import io.angularpay.onboarding.adapters.outbound.OtpServiceAdapter;
import io.angularpay.onboarding.adapters.outbound.RedisAdapter;
import io.angularpay.onboarding.configurations.AngularPayConfiguration;
import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.domain.Role;
import io.angularpay.onboarding.exceptions.CommandException;
import io.angularpay.onboarding.exceptions.ErrorObject;
import io.angularpay.onboarding.helpers.CommandHelper;
import io.angularpay.onboarding.models.AssociatedReferenceResponse;
import io.angularpay.onboarding.models.ConfirmSignupCommandRequest;
import io.angularpay.onboarding.models.ValidateOtpRequestApiModel;
import io.angularpay.onboarding.models.ValidateOtpResponseApiModel;
import io.angularpay.onboarding.ports.outbound.OtpServicePort;
import io.angularpay.onboarding.util.TTLProcessManager;
import io.angularpay.onboarding.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.angularpay.onboarding.exceptions.ErrorCode.*;
import static io.angularpay.onboarding.helpers.CommandHelper.getRequestByReferenceOrThrow;

@Service
public class ConfirmSignupCommand extends AbstractCommand<ConfirmSignupCommandRequest, AssociatedReferenceResponse>
        implements UpdatesPublisherCommand<AssociatedReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    private final OtpServicePort otpServiceAdapter;
    private final AngularPayConfiguration configuration;
    private final TTLProcessManager ttlProcessManager;

    public ConfirmSignupCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter,
            OtpServiceAdapter otpServiceAdapter,
            AngularPayConfiguration configuration,
            TTLProcessManager ttlProcessManager) {
        super("ConfirmSignupCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
        this.otpServiceAdapter = otpServiceAdapter;
        this.configuration = configuration;
        this.ttlProcessManager = ttlProcessManager;
    }

    @Override
    protected String getResourceOwner(ConfirmSignupCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected AssociatedReferenceResponse handle(ConfirmSignupCommandRequest request) {
        OnboardingRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        if (found.isSignupConfirmed()) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(REQUEST_COMPLETED_ERROR)
                    .message(REQUEST_COMPLETED_ERROR.getDefaultMessage())
                    .build();
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("x-angularpay-username", found.getUsername());
        headers.put("x-angularpay-device-id", found.getDevice().getDeviceId());
        headers.put("x-angularpay-correlation-id", request.getAuthenticatedUser().getCorrelationId());
        headers.put("x-angularpay-user-reference", "ONBOARDING-" + request.getAuthenticatedUser().getUserReference());

        Optional<ValidateOtpResponseApiModel> otpResponse = this.otpServiceAdapter.validateOtp(ValidateOtpRequestApiModel.builder()
                        .deviceReference(found.getDevice().getReference())
                        .code(request.getConfirmSignupApiModel().getOtp())
                        .build(),
                headers
        );
        if (otpResponse.isEmpty()) {
            throw CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(OTP_SERVICE_ERROR)
                    .message(OTP_SERVICE_ERROR.getDefaultMessage())
                    .build();
        }
        if (!otpResponse.get().isValid()) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(INVALID_OTP_ERROR)
                    .message(INVALID_OTP_ERROR.getDefaultMessage())
                    .build();
        }

        ScheduledFuture<?> process = Executors.newScheduledThreadPool(1).schedule(
                () -> {
                    this.mongoAdapter.findRequestByReference(found.getReference()).ifPresent(reFound -> {
                        if (!reFound.isUserOnboarded()) {
                            this.mongoAdapter.deleteOnboardingRequest(reFound);
                        }
                    });
                    this.ttlProcessManager.invalidate(found.getReference());
                },
                configuration.getOnboardingTTLMinutes(),
                TimeUnit.MINUTES
        );
        this.ttlProcessManager.put(found.getReference(), process);

        return this.commandHelper.updateProperty(found, () -> true, found::setSignupConfirmed);
    }

    @Override
    protected List<ErrorObject> validate(ConfirmSignupCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public String convertToUpdatesMessage(OnboardingRequest smartSaveRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(smartSaveRequest);
    }

    @Override
    public OnboardingRequest getByReference(AssociatedReferenceResponse associatedReferenceResponse) {
        return getRequestByReferenceOrThrow(this.mongoAdapter, associatedReferenceResponse.getRequestReference());
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }
}
