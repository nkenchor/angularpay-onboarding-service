package io.angularpay.onboarding.ports.outbound;

import io.angularpay.onboarding.models.CreateUserConfigurationRequestApiModel;
import io.angularpay.onboarding.models.ResourceCreationResponse;
import io.angularpay.onboarding.models.ResourceDeletionResponse;
import io.angularpay.onboarding.models.RollbackUserConfigurationRequestApiModel;

import java.util.Map;

public interface UserconfigServicePort {
    ResourceCreationResponse createUserConfiguration(
            CreateUserConfigurationRequestApiModel createUserConfigurationRequestApiModel,
            Map<String, String> headers);
    ResourceDeletionResponse rollbackUserConfiguration(
            RollbackUserConfigurationRequestApiModel rollbackUserConfigurationRequestApiModel,
            Map<String, String> headers);
}
