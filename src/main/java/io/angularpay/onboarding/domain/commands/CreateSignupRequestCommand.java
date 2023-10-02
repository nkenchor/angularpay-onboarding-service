package io.angularpay.onboarding.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.onboarding.adapters.outbound.MongoAdapter;
import io.angularpay.onboarding.adapters.outbound.NotificationServiceAdapter;
import io.angularpay.onboarding.adapters.outbound.OtpServiceAdapter;
import io.angularpay.onboarding.adapters.outbound.RedisAdapter;
import io.angularpay.onboarding.configurations.AngularPayConfiguration;
import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.domain.Role;
import io.angularpay.onboarding.exceptions.ErrorObject;
import io.angularpay.onboarding.helpers.CommandHelper;
import io.angularpay.onboarding.models.*;
import io.angularpay.onboarding.ports.outbound.NotificationServicePort;
import io.angularpay.onboarding.ports.outbound.OtpServicePort;
import io.angularpay.onboarding.util.TTLProcessManager;
import io.angularpay.onboarding.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.angularpay.onboarding.helpers.CommandHelper.*;
import static io.angularpay.onboarding.helpers.Helper.maskPhone;
import static io.angularpay.onboarding.helpers.Helper.tryMaskEmail;
import static io.angularpay.onboarding.helpers.ObjectFactory.onboardingRequestWithDefaults;

@Slf4j
@Service
public class CreateSignupRequestCommand extends AbstractCommand<CreateRequestCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<AssociatedReferenceResponse>,
        ResourceReferenceCommand<AssociatedReferenceResponse, ResourceReferenceResponse>,
        SensitiveDataCommand<CreateRequestCommandRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    private final OtpServicePort otpServiceAdapter;
    private final NotificationServicePort notificationServiceAdapter;
    private final AngularPayConfiguration configuration;
    private final TTLProcessManager ttlProcessManager;

    public CreateSignupRequestCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter,
            OtpServiceAdapter otpServiceAdapter,
            NotificationServiceAdapter notificationServiceAdapter,
            AngularPayConfiguration configuration,
            TTLProcessManager ttlProcessManager) {
        super("CreateSignupRequestCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
        this.otpServiceAdapter = otpServiceAdapter;
        this.notificationServiceAdapter = notificationServiceAdapter;
        this.configuration = configuration;
        this.ttlProcessManager = ttlProcessManager;
    }

    @Override
    protected String getResourceOwner(CreateRequestCommandRequest request) {
        return request.getAuthenticatedUser().getDeviceId();
    }

    @Override
    protected AssociatedReferenceResponse handle(CreateRequestCommandRequest request) {
        notExistsRequestByEmailOrThrow(this.mongoAdapter, request.getCreateSignupRequest().getEmail());
        notExistsRequestByPhoneOrThrow(this.mongoAdapter, request.getCreateSignupRequest().getPhone());
        notExistsRequestByDeviceIdOrThrow(this.mongoAdapter, request.getCreateSignupRequest().getDevice().getDeviceId());

        OnboardingRequest onboardingRequestWithDefaults = onboardingRequestWithDefaults();
        OnboardingRequest withOtherDetails = onboardingRequestWithDefaults.toBuilder()
                .phone(request.getCreateSignupRequest().getPhone())
                .device(request.getCreateSignupRequest().getDevice().toBuilder()
                        .reference(UUID.randomUUID().toString())
                        .build())
                .build();

        if (Objects.nonNull(request.getCreateSignupRequest().getEmail())) {
            withOtherDetails.setUsername(request.getCreateSignupRequest().getEmail().toLowerCase());
            withOtherDetails.setEmail(request.getCreateSignupRequest().getEmail().toLowerCase());
        }

        OnboardingRequest response = this.mongoAdapter.createRequest(withOtherDetails);

        Map<String, String> headers = new HashMap<>();
        headers.put("x-angularpay-username", request.getCreateSignupRequest().getEmail());
        headers.put("x-angularpay-device-id", request.getCreateSignupRequest().getDevice().getDeviceId());
        headers.put("x-angularpay-correlation-id", request.getAuthenticatedUser().getCorrelationId());

        CreateOtpResponseApiModel otpResponse = createOtpOrThrow(this.otpServiceAdapter, response, headers);
        sendOtpNotificationOrThrow(this.notificationServiceAdapter, response, otpResponse.getCode(), headers);

        ScheduledFuture<?> process = Executors.newScheduledThreadPool(1).schedule(
                () -> {
                    this.mongoAdapter.findRequestByReference(response.getReference()).ifPresent(found -> {
                        if (!found.isSignupConfirmed()) {
                            this.mongoAdapter.deleteOnboardingRequest(found);
                        }
                    });
                    this.ttlProcessManager.invalidate(response.getReference());
                },
                configuration.getOnboardingTTLMinutes(),
                TimeUnit.MINUTES
        );
        this.ttlProcessManager.put(response.getReference(), process);

        return AssociatedReferenceResponse.builder().requestReference(response.getReference()).build();
    }

    @Override
    protected List<ErrorObject> validate(CreateRequestCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.singletonList(Role.ROLE_ONBOARDING_USER);
    }

    @Override
    public String convertToUpdatesMessage(OnboardingRequest onboardingRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(onboardingRequest);
    }

    @Override
    public OnboardingRequest getByReference(AssociatedReferenceResponse associatedReferenceResponse) {
        return getRequestByReferenceOrThrow(this.mongoAdapter, associatedReferenceResponse.getRequestReference());
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }

    @Override
    public ResourceReferenceResponse map(AssociatedReferenceResponse associatedReferenceResponse) {
        return new ResourceReferenceResponse(associatedReferenceResponse.getRequestReference());
    }

    @Override
    public CreateRequestCommandRequest mask(CreateRequestCommandRequest raw) {
        try {
            JsonNode node = mapper.convertValue(raw, JsonNode.class);
            JsonNode createSignupRequest = node.get("createSignupRequest");
            ((ObjectNode) createSignupRequest).put("email", tryMaskEmail(raw.getCreateSignupRequest().getEmail()));
            ((ObjectNode) createSignupRequest).put("phone", maskPhone(raw.getCreateSignupRequest().getPhone()));
            return mapper.treeToValue(node, CreateRequestCommandRequest.class);
        } catch (JsonProcessingException exception) {
            return raw;
        }
    }
}
