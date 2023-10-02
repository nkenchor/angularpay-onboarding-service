package io.angularpay.onboarding.adapters.outbound.mocks;

import io.angularpay.onboarding.models.CreateUserLoginRequestApiModel;
import io.angularpay.onboarding.models.CreateUserLoginResponseApiModel;
import io.angularpay.onboarding.models.ResourceCreationResponse;
import io.angularpay.onboarding.ports.outbound.IdentityServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdentityServiceAdapterMock implements IdentityServicePort {

    @Override
    public ResourceCreationResponse createLoginAccount(
            CreateUserLoginRequestApiModel createUserLoginRequestApiModel,
            Map<String, String> headers) {
        CreateUserLoginResponseApiModel createUserLoginResponseApiModel = new CreateUserLoginResponseApiModel();
        createUserLoginResponseApiModel.setReference(UUID.randomUUID().toString());
        return ResourceCreationResponse.builder().success(true).reference(createUserLoginResponseApiModel.getReference()).build();
    }
}
