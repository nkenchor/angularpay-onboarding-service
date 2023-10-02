package io.angularpay.onboarding.adapters.outbound;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

import static io.angularpay.onboarding.common.Constants.UPDATES_TOPIC;

@Configuration
public class RedisOutboundConfiguration {

    @Bean
    ChannelTopic updatesTopic() {
        return new ChannelTopic(UPDATES_TOPIC);
    }
}
