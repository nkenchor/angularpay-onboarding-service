
package io.angularpay.onboarding.models;

import lombok.Data;

@Data
public class CreateOtpResponseApiModel {

    private String reference;
    private String code;
}
