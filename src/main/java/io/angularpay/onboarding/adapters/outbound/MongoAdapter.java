package io.angularpay.onboarding.adapters.outbound;

import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.ports.outbound.PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoAdapter implements PersistencePort {

    private final OnboardingRepository onboardingRepository;

    @Override
    public OnboardingRequest createRequest(OnboardingRequest request) {
        request.setCreatedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return onboardingRepository.save(request);
    }

    @Override
    public OnboardingRequest updateRequest(OnboardingRequest request) {
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return onboardingRepository.save(request);
    }

    @Override
    public Optional<OnboardingRequest> findRequestByReference(String reference) {
        return onboardingRepository.findByReference(reference);
    }

    @Override
    public List<OnboardingRequest> findRequestByEmail(String email) {
        return onboardingRepository.findByEmail(email);
    }

    @Override
    public List<OnboardingRequest> findRequestByPhone(String phone) {
        return onboardingRepository.findByPhone(phone);
    }

    @Override
    public List<OnboardingRequest> findRequestByDeviceId(String deviceId) {
        return onboardingRepository.findByDevice_DeviceId(deviceId);
    }

    @Override
    public Page<OnboardingRequest> listRequests(Pageable pageable) {
        return onboardingRepository.findAll(pageable);
    }

    @Override
    public void deleteOnboardingRequest(OnboardingRequest request) {
        this.onboardingRepository.delete(request);
    }

    @Override
    public long getCountByConfirmationStatus(boolean confirmed) {
        return onboardingRepository.countBySignupConfirmed(confirmed);
    }

    @Override
    public long getCountByOnboardingStatus(boolean onboarded) {
        return onboardingRepository.countByUserOnboardedAndSignupConfirmed(onboarded, true);
    }

    @Override
    public long getTotalCount() {
        return onboardingRepository.count();
    }
}
