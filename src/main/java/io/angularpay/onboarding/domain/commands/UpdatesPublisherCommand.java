package io.angularpay.onboarding.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.onboarding.adapters.outbound.RedisAdapter;
import io.angularpay.onboarding.domain.OnboardingRequest;

import java.util.Objects;

public interface UpdatesPublisherCommand<T> {

    OnboardingRequest getByReference(T t);
    RedisAdapter getRedisAdapter();

    String convertToUpdatesMessage(OnboardingRequest onboardingRequest) throws JsonProcessingException;

    default void publishUpdates(T t) throws JsonProcessingException {
        OnboardingRequest smartSaveRequest = this.getByReference(t);
        RedisAdapter redisAdapter = this.getRedisAdapter();

        if (Objects.nonNull(smartSaveRequest) && Objects.nonNull(redisAdapter)) {
            String message = this.convertToUpdatesMessage(smartSaveRequest);
            redisAdapter.publishUpdates(message);
        }
    }
}
