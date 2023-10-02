package io.angularpay.onboarding.adapters.outbound;

import io.angularpay.onboarding.domain.OnboardingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OnboardingRepository extends MongoRepository<OnboardingRequest, String> {

    Optional<OnboardingRequest> findByReference(String reference);
    List<OnboardingRequest> findByEmail(String email);
    List<OnboardingRequest> findByPhone(String phone);
    List<OnboardingRequest> findByDevice_DeviceId(String deviceId);
    Page<OnboardingRequest> findAll(Pageable pageable);
    long countBySignupConfirmed(boolean confirmed);
    long countByUserOnboardedAndSignupConfirmed(boolean onboarded, boolean confirmed);
}
