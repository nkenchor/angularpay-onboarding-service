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
public class UpdateEmailCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotNull
    private UpdateEmailApiModel updateEmailApiModel;

    UpdateEmailCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
