
package io.angularpay.onboarding.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    private String reference;
    @NotEmpty
    private String brand;
    @NotEmpty
    @JsonProperty("device_id")
    private String deviceId;
    @NotEmpty
    private String language;
    @NotEmpty
    private String model;
    @NotEmpty
    @JsonProperty("os_version")
    private String osVersion;
    @JsonProperty("session_id")
    private String sessionId;
    @NotNull
    private DeviceType type;
}
