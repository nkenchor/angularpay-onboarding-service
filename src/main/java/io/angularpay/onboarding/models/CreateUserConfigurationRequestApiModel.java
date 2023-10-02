
package io.angularpay.onboarding.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.angularpay.onboarding.domain.Device;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class CreateUserConfigurationRequestApiModel {

    @NotEmpty
    @JsonProperty("onboarding_reference")
    private String onboardingReference;

    @NotNull
    @Valid
    @JsonProperty("user_profile")
    private UserProfile userProfile;

    @NotNull
    @Valid
    private Device device;
}
