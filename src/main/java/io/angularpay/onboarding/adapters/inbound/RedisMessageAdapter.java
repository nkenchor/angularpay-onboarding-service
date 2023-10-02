package io.angularpay.onboarding.adapters.inbound;

import io.angularpay.onboarding.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.onboarding.models.platform.PlatformConfigurationIdentifier;
import io.angularpay.onboarding.ports.inbound.InboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.angularpay.onboarding.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}
