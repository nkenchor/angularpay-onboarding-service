package io.angularpay.onboarding.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.onboarding.adapters.outbound.MongoAdapter;
import io.angularpay.onboarding.configurations.AngularPayConfiguration;
import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.exceptions.CommandException;
import io.angularpay.onboarding.exceptions.ErrorCode;
import io.angularpay.onboarding.models.*;
import io.angularpay.onboarding.ports.outbound.NotificationServicePort;
import io.angularpay.onboarding.ports.outbound.OtpServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.angularpay.onboarding.common.Constants.OTP_NOTIFICATION_SENDER;
import static io.angularpay.onboarding.exceptions.ErrorCode.*;
import static io.angularpay.onboarding.models.NotificationChannel.EMAIL;
import static io.angularpay.onboarding.models.NotificationChannel.SMS;
import static io.angularpay.onboarding.models.NotificationType.INSTANT;

@Service
@RequiredArgsConstructor
public class CommandHelper {

    private final MongoAdapter mongoAdapter;
    private final ObjectMapper mapper;
    private final AngularPayConfiguration configuration;

    public AssociatedReferenceResponse executeAcid(Supplier<AssociatedReferenceResponse> supplier) {
        int maxRetry = this.configuration.getMaxUpdateRetry();
        OptimisticLockingFailureException optimisticLockingFailureException;
        int counter = 0;
        //noinspection ConstantConditions
        do {
            try {
                return supplier.get();
            } catch (OptimisticLockingFailureException exception) {
                if (counter++ >= maxRetry) throw exception;
                optimisticLockingFailureException = exception;
            }
        }
        while (Objects.nonNull(optimisticLockingFailureException));
        throw optimisticLockingFailureException;
    }

    public String getRequestOwner(String requestReference) {
        OnboardingRequest found = this.mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        return found.getDevice().getDeviceId();
    }

    private static CommandException commandException(HttpStatus status, ErrorCode errorCode) {
        return CommandException.builder()
                .status(status)
                .errorCode(errorCode)
                .message(errorCode.getDefaultMessage())
                .build();
    }

    public <T> AssociatedReferenceResponse updateProperty(OnboardingRequest onboardingRequest, Supplier<T> getter, Consumer<T> setter) {
        setter.accept(getter.get());
        OnboardingRequest response = this.mongoAdapter.updateRequest(onboardingRequest);
        return AssociatedReferenceResponse.builder().requestReference(response.getReference()).build();
    }

    public <T> String toJsonString(T t) throws JsonProcessingException {
        return this.mapper.writeValueAsString(t);
    }

    public static OnboardingRequest getRequestByReferenceOrThrow(MongoAdapter mongoAdapter, String requestReference) {
        return mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
    }

    public static void notExistsRequestByEmailOrThrow(MongoAdapter mongoAdapter, String email) {
        if (!StringUtils.hasText(email)) return;
        if (!mongoAdapter.findRequestByEmail(email).isEmpty()) {
            throw commandException(HttpStatus.CONFLICT, DUPLICATE_EMAIL_ERROR);
        }
    }

    public static void notExistsRequestByPhoneOrThrow(MongoAdapter mongoAdapter, String phone) {
        if (!StringUtils.hasText(phone)) return;
        if (!mongoAdapter.findRequestByPhone(phone).isEmpty()) {
            throw commandException(HttpStatus.CONFLICT, DUPLICATE_PHONE_ERROR);
        }
    }

    public static void notExistsRequestByDeviceIdOrThrow(MongoAdapter mongoAdapter, String deviceId) {
        if (!mongoAdapter.findRequestByDeviceId(deviceId).isEmpty()) {
            throw commandException(HttpStatus.CONFLICT, DUPLICATE_DEVICE_ERROR);
        }
    }

    public static CreateOtpResponseApiModel createOtpOrThrow(
            OtpServicePort otpServiceAdapter,
            OnboardingRequest response,
            Map<String, String> headers) {
        Optional<CreateOtpResponseApiModel> otpResponse = otpServiceAdapter.createOtp(CreateOtpRequestApiModel.builder()
                        .deviceReference(response.getDevice().getReference())
                        .strict(false)
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
        return otpResponse.get();
    }

    public static void sendOtpNotificationOrThrow(
            NotificationServicePort notificationServiceAdapter,
            OnboardingRequest response, String otp,
            Map<String, String> headers) {
//        String message = String.format(OTP_NOTIFICATION_TEMPLATE, otp);
        // TODO: remove when we register with carriers
        String message = "Test OTP: " + otp;

        headers.put("x-angularpay-user-reference", "ONBOARDING-" + response.getReference());

        List<Supplier<Optional<SendNotificationResponseApiModel>>> notifications = Arrays.asList(
                () -> notificationServiceAdapter.sendNotification(SendNotificationRequestApiModel.builder()
                                .clientReference(UUID.randomUUID().toString())
                                .channel(SMS)
                                .type(INSTANT)
                                .from(OTP_NOTIFICATION_SENDER)
                                .to(response.getPhone())
                                .message(message)
                                .build(),
                        headers),
                () -> notificationServiceAdapter.sendNotification(SendNotificationRequestApiModel.builder()
                                .clientReference(UUID.randomUUID().toString())
                                .channel(EMAIL)
                                .type(INSTANT)
                                .subject("AngularPay Service Manager Authentication")
                                .from(OTP_NOTIFICATION_SENDER)
                                .to(response.getEmail())
                                .message(message)
                                .build(),
                        headers)
        );

        if (notifications.parallelStream().map(Supplier::get).allMatch(Optional::isEmpty)) {
            throw CommandException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .errorCode(NOTIFICATION_SERVICE_ERROR)
                    .message(NOTIFICATION_SERVICE_ERROR.getDefaultMessage())
                    .build();
        }
    }
}
