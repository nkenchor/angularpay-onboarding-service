package io.angularpay.onboarding.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("angularpay")
@Data
public class AngularPayConfiguration {

    private String otpUrl;
    private String userconfigUrl;
    private String identityUrl;
    private String notificationUrl;
    private int pageSize;
    private int codecSizeInMB;
    private int maxUpdateRetry;
    private int onboardingTTLMinutes;
    private Redis redis;
    private BruteForceGuardConfiguration bruteForceGuard;

    @Data
    public static class Redis {
        private String host;
        private int port;
        private int timeout;
    }

    @Data
    public static class BruteForceGuardConfiguration {
        private int maxLoginAttempts;
        private int blockDurationInHours;
    }
}
