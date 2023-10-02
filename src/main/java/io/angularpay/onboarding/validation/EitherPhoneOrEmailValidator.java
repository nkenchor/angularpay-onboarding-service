package io.angularpay.onboarding.validation;

import io.angularpay.onboarding.models.CreateSignupRequest;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

import static io.angularpay.onboarding.common.Constants.REGEX_EMAIL_ADDRESS;
import static io.angularpay.onboarding.common.Constants.REGEX_INTERNATIONAL_PHONE_NUMBER;

public class EitherPhoneOrEmailValidator implements ConstraintValidator<EitherPhoneOrEmail, CreateSignupRequest> {

    private final Pattern phonePattern = Pattern.compile(REGEX_INTERNATIONAL_PHONE_NUMBER);
    private final Pattern emailPattern = Pattern.compile(REGEX_EMAIL_ADDRESS, Pattern.CASE_INSENSITIVE);

    public void initialize(EitherPhoneOrEmail constraint) {
    }

    public boolean isValid(CreateSignupRequest createSignupRequest, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(createSignupRequest.getPhone()) && !StringUtils.hasText(createSignupRequest.getEmail())) {
            return false;
        }

        if (!StringUtils.hasText(createSignupRequest.getPhone()) && StringUtils.hasText(createSignupRequest.getEmail())) {
            return emailPattern.matcher(createSignupRequest.getEmail()).matches();
        }

        if (StringUtils.hasText(createSignupRequest.getPhone()) && !StringUtils.hasText(createSignupRequest.getEmail())) {
            return phonePattern.matcher(createSignupRequest.getPhone()).matches();
        }

        if (StringUtils.hasText(createSignupRequest.getPhone()) && StringUtils.hasText(createSignupRequest.getEmail())) {
            return phonePattern.matcher(createSignupRequest.getPhone()).matches()
                    && emailPattern.matcher(createSignupRequest.getEmail()).matches();
        }

        return false;
    }
}
