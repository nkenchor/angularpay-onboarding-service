package io.angularpay.onboarding.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ResendOTPCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    ResendOTPCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
