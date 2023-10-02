package io.angularpay.onboarding.ports.outbound;

import io.angularpay.onboarding.models.CreateOtpRequestApiModel;
import io.angularpay.onboarding.models.CreateOtpResponseApiModel;
import io.angularpay.onboarding.models.ValidateOtpRequestApiModel;
import io.angularpay.onboarding.models.ValidateOtpResponseApiModel;

import java.util.Map;
import java.util.Optional;

public interface OtpServicePort {
    Optional<CreateOtpResponseApiModel> createOtp(
            CreateOtpRequestApiModel createOtpRequestApiModel,
            Map<String, String> headers);
    Optional<ValidateOtpResponseApiModel> validateOtp(
            ValidateOtpRequestApiModel validateOtpRequestApiModel,
            Map<String, String> headers);
}
