package io.angularpay.onboarding.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BruteForceCacheEntry {

    private boolean blocked;
    private int loginAttempts;
    private String blockedAt;
}
