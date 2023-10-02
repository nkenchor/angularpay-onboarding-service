package io.angularpay.onboarding.exceptions;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_MESSAGE_ERROR("The message format read from the given topic is invalid"),
    VALIDATION_ERROR("The request has validation errors"),
    REQUEST_COMPLETED_ERROR("You cannot performed this action on a request that has already been completed"),
    MISSING_USERNAME_ERROR("Username is required if the User only signed up with a phone number and hasn't provided email yet"),
    ILLEGAL_REQUEST_ERROR("You cannot performed this action on a request that has NOT been confirmed"),
    REQUEST_NOT_FOUND("The requested resource was NOT found"),
    OTP_SERVICE_ERROR("Unable to generate OTP. Please check otp-service logs for details."),
    NOTIFICATION_SERVICE_ERROR("Unable to send notification. Please check notification-service logs for details."),
    USERCONFIG_SERVICE_ERROR("Unable to create User Configuration Account. Please check userconfig-service logs for details."),
    IDENTITY_SERVICE_ERROR("Unable to create User Login Account. Please check identity-service logs for details."),
    INVALID_OTP_ERROR("The OTP you entered is invalid."),
    DUPLICATE_REQUEST_ERROR("A resource having the same identifier already exist"),
    DUPLICATE_EMAIL_ERROR("A resource having the same email address already exist"),
    DUPLICATE_PHONE_ERROR("A resource having the same phone number already exist"),
    DUPLICATE_DEVICE_ERROR("A resource having the same device identifier already exist"),
    GENERIC_ERROR("Generic error occurred. See stacktrace for details"),
    AUTHORIZATION_ERROR("You do NOT have adequate permission to access this resource"),
    BLOCKED_IP_ERROR("This IP has been blocked!"),
    MISSING_IP_ERROR("There is no IP to identify the remote client!"),
    NO_PRINCIPAL("Principal identifier NOT provided", 500);

    private final String defaultMessage;
    private final int defaultHttpStatus;

    ErrorCode(String defaultMessage) {
        this(defaultMessage, 400);
    }

    ErrorCode(String defaultMessage, int defaultHttpStatus) {
        this.defaultMessage = defaultMessage;
        this.defaultHttpStatus = defaultHttpStatus;
    }
}
