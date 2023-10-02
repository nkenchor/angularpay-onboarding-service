package io.angularpay.onboarding.ports.inbound;

import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.models.*;

import java.util.List;
import java.util.Map;

public interface RestApiPort {
    GenericReferenceResponse create(CreateSignupRequest request, Map<String, String> headers);
    void confirmSignup(String requestReference, ConfirmSignupApiModel confirmSignupApiModel, Map<String, String> headers);
    void updateEmail(String requestReference, UpdateEmailApiModel updateEmailApiModel, Map<String, String> headers);
    void updatePhone(String requestReference, UpdatePhoneNumberApiModel updatePhoneNumberApiModel, Map<String, String> headers);
    GenericReferenceResponse onboardUser(String requestReference, OnboardUserApiModel onboardUserApiModel, Map<String, String> headers);
    OnboardingRequest getSignupRequestByReference(String requestReference, Map<String, String> headers);
    List<OnboardingRequest> getSignupRequestList(int page, Map<String, String> headers);
    List<Statistics> getStatistics(Map<String, String> headers);
    void resendOTP(String requestReference, Map<String, String> headers);
}
