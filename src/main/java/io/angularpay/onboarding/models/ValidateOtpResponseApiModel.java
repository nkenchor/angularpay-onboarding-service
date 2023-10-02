
package io.angularpay.onboarding.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ValidateOtpResponseApiModel {

    @JsonProperty("is_valid")
    private boolean valid;
}
