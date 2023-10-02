package io.angularpay.onboarding.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.onboarding.adapters.outbound.MongoAdapter;
import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.domain.Role;
import io.angularpay.onboarding.exceptions.ErrorObject;
import io.angularpay.onboarding.helpers.CommandHelper;
import io.angularpay.onboarding.models.GetRequestByReferenceCommandRequest;
import io.angularpay.onboarding.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static io.angularpay.onboarding.helpers.CommandHelper.getRequestByReferenceOrThrow;

@Service
public class GetRequestByReferenceCommand extends AbstractCommand<GetRequestByReferenceCommandRequest, OnboardingRequest> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;

    public GetRequestByReferenceCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper) {
        super("GetRequestByReferenceCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
    }

    @Override
    protected String getResourceOwner(GetRequestByReferenceCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected OnboardingRequest handle(GetRequestByReferenceCommandRequest request) {
        return getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
    }

    @Override
    protected List<ErrorObject> validate(GetRequestByReferenceCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_PLATFORM_ADMIN, Role.ROLE_PLATFORM_USER);
    }
}
