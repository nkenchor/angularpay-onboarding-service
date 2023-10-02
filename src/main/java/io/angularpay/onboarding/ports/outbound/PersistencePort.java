package io.angularpay.onboarding.ports.outbound;

import io.angularpay.onboarding.domain.OnboardingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PersistencePort {
    OnboardingRequest createRequest(OnboardingRequest request);
    OnboardingRequest updateRequest(OnboardingRequest request);
    Optional<OnboardingRequest> findRequestByReference(String reference);
    List<OnboardingRequest> findRequestByEmail(String email);
    List<OnboardingRequest> findRequestByPhone(String phone);
    List<OnboardingRequest> findRequestByDeviceId(String deviceId);
    Page<OnboardingRequest> listRequests(Pageable pageable);
    void deleteOnboardingRequest(OnboardingRequest request);
    long getCountByConfirmationStatus(boolean confirmed);
    long getCountByOnboardingStatus(boolean onboarded);
    long getTotalCount();
}
