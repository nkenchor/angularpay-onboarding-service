
package io.angularpay.onboarding.models;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class AssociatedReferenceResponse extends GenericReferenceResponse {

    private final String requestReference;
    private final String userReference;
}
