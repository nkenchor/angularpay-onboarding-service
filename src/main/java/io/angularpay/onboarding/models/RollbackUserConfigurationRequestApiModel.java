
package io.angularpay.onboarding.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
public class RollbackUserConfigurationRequestApiModel {

    @NotEmpty
    private String userReference;

    @NotEmpty
    private String username;

    @NotEmpty
    @JsonProperty("device_id")
    private String deviceId;
}
