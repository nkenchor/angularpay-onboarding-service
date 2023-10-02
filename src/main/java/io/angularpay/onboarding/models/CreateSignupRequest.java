
package io.angularpay.onboarding.models;

import io.angularpay.onboarding.domain.Device;
import io.angularpay.onboarding.validation.EitherPhoneOrEmail;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EitherPhoneOrEmail
public class CreateSignupRequest {

    private String phone;
    private String email;
    @NotNull
    @Valid
    private Device device;
}
