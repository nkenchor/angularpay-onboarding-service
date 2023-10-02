package io.angularpay.onboarding.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class UpdateEmailApiModel {

    @NotEmpty
    private String email;
}
