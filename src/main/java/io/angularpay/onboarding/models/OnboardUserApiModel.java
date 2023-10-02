package io.angularpay.onboarding.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class OnboardUserApiModel {

    @NotEmpty
    private String firstname;
    @NotEmpty
    private String lastname;
    private String username;
    @NotEmpty
    private String password;
}
