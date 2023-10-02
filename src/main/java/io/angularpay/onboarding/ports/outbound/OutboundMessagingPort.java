package io.angularpay.onboarding.ports.outbound;

import java.util.Map;

public interface OutboundMessagingPort {
    void publishUpdates(String message);
    Map<String, String> getPlatformConfigurations(String hashName);
}
