
package io.angularpay.onboarding.models;

import io.angularpay.onboarding.exceptions.ErrorCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceDeletionResponse {

    private boolean success;
    private ErrorCode errorCode;
}
