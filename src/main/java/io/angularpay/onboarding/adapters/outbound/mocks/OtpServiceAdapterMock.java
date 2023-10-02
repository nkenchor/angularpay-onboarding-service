package io.angularpay.onboarding.adapters.outbound.mocks;

import io.angularpay.onboarding.models.CreateOtpRequestApiModel;
import io.angularpay.onboarding.models.CreateOtpResponseApiModel;
import io.angularpay.onboarding.models.ValidateOtpRequestApiModel;
import io.angularpay.onboarding.models.ValidateOtpResponseApiModel;
import io.angularpay.onboarding.ports.outbound.OtpServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpServiceAdapterMock implements OtpServicePort {

    @Override
    public Optional<CreateOtpResponseApiModel> createOtp(
            CreateOtpRequestApiModel createOtpRequestApiModel,
            Map<String, String> headers) {
        CreateOtpResponseApiModel createOtpResponseApiModel = new CreateOtpResponseApiModel();
        createOtpResponseApiModel.setReference(UUID.randomUUID().toString());
        createOtpResponseApiModel.setCode("12345");
        return Optional.of(createOtpResponseApiModel);
    }

    @Override
    public Optional<ValidateOtpResponseApiModel> validateOtp(
            ValidateOtpRequestApiModel validateOtpRequestApiModel,
            Map<String, String> headers) {
        ValidateOtpResponseApiModel validateOtpResponseApiModel = new ValidateOtpResponseApiModel();
        validateOtpResponseApiModel.setValid(true);
        return Optional.of(validateOtpResponseApiModel);
    }
}
