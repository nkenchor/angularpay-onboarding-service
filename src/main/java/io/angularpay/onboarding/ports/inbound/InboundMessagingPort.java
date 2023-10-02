package io.angularpay.onboarding.ports.inbound;

import io.angularpay.onboarding.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}
