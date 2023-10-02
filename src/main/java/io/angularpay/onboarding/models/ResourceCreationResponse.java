
package io.angularpay.onboarding.models;

import io.angularpay.onboarding.exceptions.ErrorCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceCreationResponse {

    private boolean success;
    private String reference;
    private ErrorCode errorCode;
}
