package io.angularpay.onboarding.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.angularpay.onboarding.adapters.outbound.*;
import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.domain.Role;
import io.angularpay.onboarding.exceptions.CommandException;
import io.angularpay.onboarding.exceptions.ErrorObject;
import io.angularpay.onboarding.helpers.CommandHelper;
import io.angularpay.onboarding.models.*;
import io.angularpay.onboarding.ports.outbound.IdentityServicePort;
import io.angularpay.onboarding.ports.outbound.NotificationServicePort;
import io.angularpay.onboarding.ports.outbound.UserconfigServicePort;
import io.angularpay.onboarding.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Supplier;

import static io.angularpay.onboarding.common.Constants.ANGULARPAY_WELCOME_MESSAGE_PLACEHOLDER;
import static io.angularpay.onboarding.common.Constants.OTP_NOTIFICATION_SENDER;
import static io.angularpay.onboarding.exceptions.ErrorCode.*;
import static io.angularpay.onboarding.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.onboarding.helpers.Helper.maskEmail;
import static io.angularpay.onboarding.models.NotificationChannel.EMAIL;
import static io.angularpay.onboarding.models.NotificationChannel.SMS;
import static io.angularpay.onboarding.models.NotificationType.INSTANT;

@Service
public class OnboardUserCommand extends AbstractCommand<OnboardUserCommandRequest, AssociatedReferenceResponse>
        implements UpdatesPublisherCommand<AssociatedReferenceResponse>,
        ResourceReferenceCommand<AssociatedReferenceResponse, ResourceReferenceResponse>,
        SensitiveDataCommand<OnboardUserCommandRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    private final UserconfigServicePort userconfigServiceAdapter;
    private final IdentityServicePort identityServiceAdapter;
    private final NotificationServicePort notificationServiceAdapter;

    public OnboardUserCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter,
            UserconfigServiceAdapter userconfigServiceAdapter,
            IdentityServiceAdapter identityServiceAdapter,
            NotificationServiceAdapter notificationServiceAdapter) {
        super("OnboardUserCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
        this.userconfigServiceAdapter = userconfigServiceAdapter;
        this.identityServiceAdapter = identityServiceAdapter;
        this.notificationServiceAdapter = notificationServiceAdapter;
    }

    @Override
    protected String getResourceOwner(OnboardUserCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected AssociatedReferenceResponse handle(OnboardUserCommandRequest request) {
        OnboardingRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        if (!found.isSignupConfirmed()) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(ILLEGAL_REQUEST_ERROR)
                    .message(ILLEGAL_REQUEST_ERROR.getDefaultMessage())
                    .build();
        }

        if (!StringUtils.hasText(request.getOnboardUserApiModel().getUsername()) && !StringUtils.hasText(found.getUsername())) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(MISSING_USERNAME_ERROR)
                    .message(MISSING_USERNAME_ERROR.getDefaultMessage())
                    .build();
        }

        if (StringUtils.hasText(request.getOnboardUserApiModel().getUsername()) && !StringUtils.hasText(found.getUsername())) {
            this.commandHelper.updateProperty(found, request.getOnboardUserApiModel()::getUsername, found::setUsername);
            this.commandHelper.updateProperty(found, request.getOnboardUserApiModel()::getUsername, found::setEmail);
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("x-angularpay-username", found.getUsername());
        headers.put("x-angularpay-device-id", found.getDevice().getDeviceId());
        headers.put("x-angularpay-correlation-id", request.getAuthenticatedUser().getCorrelationId());
        headers.put("x-angularpay-user-roles", String.join(",", Arrays.asList(Role.ROLE_USERCONFIG_ADMIN.name(), Role.ROLE_IDENTITY_ADMIN.name())));

        ResourceCreationResponse userResourceCreationResponse = createUserConfiguration(request, found, headers);

        if (!userResourceCreationResponse.isSuccess()) {
            if (userResourceCreationResponse.getErrorCode() == DUPLICATE_REQUEST_ERROR) {
                throw CommandException.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .errorCode(DUPLICATE_REQUEST_ERROR)
                        .message(DUPLICATE_REQUEST_ERROR.getDefaultMessage())
                        .build();
            } else {
                throw CommandException.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .errorCode(USERCONFIG_SERVICE_ERROR)
                        .message(USERCONFIG_SERVICE_ERROR.getDefaultMessage())
                        .build();
            }
        }

        headers.put("x-angularpay-user-reference", userResourceCreationResponse.getReference());

        ResourceCreationResponse identityResourceCreationResponse = createLoginAccount(
                userResourceCreationResponse.getReference(),
                request,
                found,
                headers
        );

        if (!identityResourceCreationResponse.isSuccess()) {
            // rollback user configuration first!
            ResourceDeletionResponse rollbackResponse = rollbackUserConfiguration(found, userResourceCreationResponse, headers);

            if (identityResourceCreationResponse.getErrorCode() == DUPLICATE_REQUEST_ERROR) {
                throw CommandException.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .errorCode(DUPLICATE_REQUEST_ERROR)
                        .message(DUPLICATE_REQUEST_ERROR.getDefaultMessage())
                        .build();
            }

            if (!rollbackResponse.isSuccess()) {
                // TODO: consider using a scheduled job to rollback
                throw CommandException.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .errorCode(USERCONFIG_SERVICE_ERROR)
                        .message(USERCONFIG_SERVICE_ERROR.getDefaultMessage())
                        .build();
            }

            throw CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(IDENTITY_SERVICE_ERROR)
                    .message(IDENTITY_SERVICE_ERROR.getDefaultMessage())
                    .build();
        }

        this.sendNotification(found, headers);

        found.setUserOnboarded(true);
        this.mongoAdapter.updateRequest(found);

        return AssociatedReferenceResponse.builder()
                .requestReference(found.getReference())
                .userReference(userResourceCreationResponse.getReference())
                .build();
    }

    @Override
    protected List<ErrorObject> validate(OnboardUserCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
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
    public ResourceReferenceResponse map(AssociatedReferenceResponse referenceResponse) {
        return new ResourceReferenceResponse(referenceResponse.getUserReference());
    }

    private void sendNotification(OnboardingRequest found, Map<String, String> headers) {
        List<Supplier<Optional<SendNotificationResponseApiModel>>> notifications = Arrays.asList(
                // TODO consider publishing this to redis instead
                () -> this.notificationServiceAdapter.sendNotification(SendNotificationRequestApiModel.builder()
                                .clientReference(UUID.randomUUID().toString())
                                .channel(SMS)
                                .type(INSTANT)
                                .from(OTP_NOTIFICATION_SENDER)
                                .to(found.getPhone())
                                .message(ANGULARPAY_WELCOME_MESSAGE_PLACEHOLDER)
                                .build(),
                        headers),
                // TODO consider publishing this to redis instead
                () -> this.notificationServiceAdapter.sendNotification(SendNotificationRequestApiModel.builder()
                                .clientReference(UUID.randomUUID().toString())
                                .channel(EMAIL)
                                .type(INSTANT)
                                .subject("Welcome to the AngularPay Family!")
                                .from(OTP_NOTIFICATION_SENDER)
                                .to(found.getEmail())
                                .message(ANGULARPAY_WELCOME_MESSAGE_PLACEHOLDER)
                                .build(),
                        headers)
        );
        notifications.stream().parallel().forEach(Supplier::get);
    }

    private ResourceDeletionResponse rollbackUserConfiguration(
            OnboardingRequest found,
            ResourceCreationResponse userResourceCreationResponse,
            Map<String, String> headers) {
        return this.userconfigServiceAdapter.rollbackUserConfiguration(RollbackUserConfigurationRequestApiModel.builder()
                        .username(found.getUsername())
                        .userReference(userResourceCreationResponse.getReference())
                        .deviceId(found.getDevice().getDeviceId())
                        .build(),
                headers
        );
    }

    private ResourceCreationResponse createLoginAccount(
            String userReference,
            OnboardUserCommandRequest request,
            OnboardingRequest found,
            Map<String, String> headers) {
        return this.identityServiceAdapter.createLoginAccount(
                CreateUserLoginRequestApiModel.builder()
                        .userReference(userReference)
                        .firstname(request.getOnboardUserApiModel().getFirstname())
                        .lastname(request.getOnboardUserApiModel().getLastname())
                        .email(found.getUsername())
                        .username(found.getUsername())
                        .password(request.getOnboardUserApiModel().getPassword())
                        .device(found.getDevice().getDeviceId())
                        .build(),
                headers
        );
    }

    private ResourceCreationResponse createUserConfiguration(
            OnboardUserCommandRequest request,
            OnboardingRequest found,
            Map<String, String> headers) {
        return this.userconfigServiceAdapter.createUserConfiguration(
                CreateUserConfigurationRequestApiModel.builder()
                        .onboardingReference(found.getReference())
                        .userProfile(UserProfile.builder()
                                .handle(found.getUsername()) // TODO generate unique handle
                                .email(found.getUsername())
                                .username(found.getUsername())
                                .firstname(request.getOnboardUserApiModel().getFirstname())
                                .lastname(request.getOnboardUserApiModel().getLastname())
                                .phone(found.getPhone())
                                .build())
                        .device(found.getDevice())
                        .build(),
                headers
        );
    }

    @Override
    public OnboardUserCommandRequest mask(OnboardUserCommandRequest raw) {
        try {
            JsonNode node = mapper.convertValue(raw, JsonNode.class);
            JsonNode userLoginApiModel = node.get("onboardUserApiModel");
            ((ObjectNode) userLoginApiModel).put("username", maskEmail(raw.getOnboardUserApiModel().getUsername()));
            ((ObjectNode) userLoginApiModel).put("password", "*****");
            return mapper.treeToValue(node, OnboardUserCommandRequest.class);
        } catch (JsonProcessingException exception) {
            return raw;
        }
    }
}
