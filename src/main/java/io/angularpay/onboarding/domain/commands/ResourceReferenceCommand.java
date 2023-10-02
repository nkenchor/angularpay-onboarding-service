package io.angularpay.onboarding.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}
