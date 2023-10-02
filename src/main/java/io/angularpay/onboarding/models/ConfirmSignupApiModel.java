package io.angularpay.onboarding.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ConfirmSignupApiModel {

    @NotEmpty
    private String otp;
}
