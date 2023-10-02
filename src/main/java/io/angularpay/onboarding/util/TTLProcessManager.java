package io.angularpay.onboarding.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.angularpay.onboarding.configurations.AngularPayConfiguration;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TTLProcessManager {

    private final Cache<String, ScheduledFuture<?>> processes;

    public TTLProcessManager(AngularPayConfiguration configuration) {
        this.processes = CacheBuilder.newBuilder()
                .expireAfterWrite(configuration.getOnboardingTTLMinutes(), TimeUnit.MINUTES)
                .build();
    }

    public void put(String reference, ScheduledFuture<?> process) {
        ScheduledFuture<?> entry = processes.getIfPresent(reference);
        if (Objects.nonNull(entry)) {
            if (!entry.isCancelled() && !entry.isDone()) {
                entry.cancel(true);
            }
        }
        processes.put(reference, process);
    }

    public void invalidate(String reference) {
        ScheduledFuture<?> entry = processes.getIfPresent(reference);
        if (Objects.nonNull(entry)) {
            processes.invalidate(reference);
        }
    }
}
