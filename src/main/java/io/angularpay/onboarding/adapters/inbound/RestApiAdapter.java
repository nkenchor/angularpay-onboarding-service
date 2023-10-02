package io.angularpay.onboarding.adapters.inbound;

import io.angularpay.onboarding.configurations.AngularPayConfiguration;
import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.domain.commands.*;
import io.angularpay.onboarding.models.*;
import io.angularpay.onboarding.ports.inbound.RestApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.angularpay.onboarding.helpers.Helper.fromHeaders;

@RestController
@RequestMapping("/onboarding/signups")
@RequiredArgsConstructor
public class RestApiAdapter implements RestApiPort {

    private final AngularPayConfiguration configuration;

    private final CreateSignupRequestCommand createSignupRequestCommand;
    private final ConfirmSignupCommand confirmSignupCommand;
    private final UpdateEmailCommand updateEmailCommand;
    private final UpdatePhoneNumberCommand updatePhoneNumberCommand;
    private final OnboardUserCommand onboardUserCommand;
    private final GetRequestByReferenceCommand getRequestByReferenceCommand;
    private final GetSignupRequestListCommand getSignupRequestListCommand;
    private final ResendOTPCommand resendOTPCommand;
    private final GetStatisticsCommand getStatisticsCommand;

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse create(
            @RequestBody CreateSignupRequest request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        CreateRequestCommandRequest createRequestCommandRequest = CreateRequestCommandRequest.builder()
                .createSignupRequest(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return createSignupRequestCommand.execute(createRequestCommandRequest);
    }

    @PutMapping("/{requestReference}/confirm-signup")
    @Override
    public void confirmSignup(
            @PathVariable String requestReference,
            @RequestBody ConfirmSignupApiModel confirmSignupApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ConfirmSignupCommandRequest confirmSignupCommandRequest = ConfirmSignupCommandRequest.builder()
                .requestReference(requestReference)
                .confirmSignupApiModel(confirmSignupApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.confirmSignupCommand.execute(confirmSignupCommandRequest);
    }

    @PutMapping("/{requestReference}/email")
    @Override
    public void updateEmail(
            @PathVariable String requestReference,
            @RequestBody UpdateEmailApiModel updateEmailApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateEmailCommandRequest updateEmailCommandRequest = UpdateEmailCommandRequest.builder()
                .requestReference(requestReference)
                .updateEmailApiModel(updateEmailApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updateEmailCommand.execute(updateEmailCommandRequest);
    }

    @PutMapping("/{requestReference}/phone")
    @Override
    public void updatePhone(
            @PathVariable String requestReference,
            @RequestBody UpdatePhoneNumberApiModel updatePhoneNumberApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdatePhoneNumberCommandRequest updatePhoneNumberCommandRequest = UpdatePhoneNumberCommandRequest.builder()
                .requestReference(requestReference)
                .updatePhoneNumberApiModel(updatePhoneNumberApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        this.updatePhoneNumberCommand.execute(updatePhoneNumberCommandRequest);
    }

    @PostMapping("/{requestReference}/onboard")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse onboardUser(
            @PathVariable String requestReference,
            @RequestBody OnboardUserApiModel onboardUserApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        OnboardUserCommandRequest onboardUserCommandRequest = OnboardUserCommandRequest.builder()
                .requestReference(requestReference)
                .onboardUserApiModel(onboardUserApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.onboardUserCommand.execute(onboardUserCommandRequest);
    }

    @GetMapping("/{requestReference}")
    @Override
    public OnboardingRequest getSignupRequestByReference(
            @PathVariable String requestReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetRequestByReferenceCommandRequest getRequestByReferenceCommandRequest = GetRequestByReferenceCommandRequest.builder()
                .requestReference(requestReference)
                .authenticatedUser(authenticatedUser)
                .build();
        return this.getRequestByReferenceCommand.execute(getRequestByReferenceCommandRequest);
    }

    @GetMapping("/list/page/{page}")
    @Override
    public List<OnboardingRequest> getSignupRequestList(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetSignupRequestListCommandRequest getSignupRequestListCommandRequest = GetSignupRequestListCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return this.getSignupRequestListCommand.execute(getSignupRequestListCommandRequest);
    }

    @PostMapping("/{requestReference}/resend-otp")
    @Override
    public void resendOTP(
            @PathVariable String requestReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ResendOTPCommandRequest resendOTPCommandRequest = ResendOTPCommandRequest.builder()
                .requestReference(requestReference)
                .authenticatedUser(authenticatedUser)
                .build();
        this.resendOTPCommand.execute(resendOTPCommandRequest);
    }

    @GetMapping("/statistics")
    @ResponseBody
    @Override
    public List<Statistics> getStatistics(@RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetStatisticsCommandRequest getStatisticsCommandRequest = GetStatisticsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .build();
        return getStatisticsCommand.execute(getStatisticsCommandRequest);
    }
}
