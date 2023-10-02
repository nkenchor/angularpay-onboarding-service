
package io.angularpay.onboarding.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserLoginRequestApiModel {

    private String device;
    private String email;
    private String firstname;
    private String lastname;
    private String password;
    private String username;
    @JsonProperty("user_reference")
    private String userReference;
}
