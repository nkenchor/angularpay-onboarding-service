package io.angularpay.onboarding.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdatePhoneNumberCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotNull
    private UpdatePhoneNumberApiModel updatePhoneNumberApiModel;

    UpdatePhoneNumberCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
