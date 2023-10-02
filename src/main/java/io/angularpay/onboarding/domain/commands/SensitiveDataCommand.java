package io.angularpay.onboarding.domain.commands;

public interface SensitiveDataCommand<T> {
    T mask(T raw);
}
