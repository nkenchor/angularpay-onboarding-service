package io.angularpay.onboarding.domain.commands;

public interface BruteForceGuardCommand {

    void onLoginSuccess(String clientIp);
    void onLoginFailure(String clientIp);
    boolean isBlocked(String clientIp);
}
