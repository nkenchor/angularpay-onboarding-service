package io.angularpay.onboarding.ports.outbound;

import io.angularpay.onboarding.models.CreateUserLoginRequestApiModel;
import io.angularpay.onboarding.models.ResourceCreationResponse;

import java.util.Map;

public interface IdentityServicePort {
    ResourceCreationResponse createLoginAccount(
            CreateUserLoginRequestApiModel createUserLoginRequestApiModel,
            Map<String, String> headers);
}
