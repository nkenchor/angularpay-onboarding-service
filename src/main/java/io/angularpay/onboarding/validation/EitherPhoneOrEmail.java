package io.angularpay.onboarding.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EitherPhoneOrEmailValidator.class)
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EitherPhoneOrEmail {
    String message() default "Please provide a valid phone number or email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
