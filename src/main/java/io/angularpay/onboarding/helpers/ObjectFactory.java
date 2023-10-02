package io.angularpay.onboarding.helpers;

import io.angularpay.onboarding.domain.OnboardingRequest;

import java.util.UUID;

public class ObjectFactory {

    public static OnboardingRequest onboardingRequestWithDefaults() {
        return OnboardingRequest.builder()
                .reference(UUID.randomUUID().toString())
                .signupConfirmed(false)
                .userOnboarded(false)
                .build();
    }
}