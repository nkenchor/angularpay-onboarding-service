package io.angularpay.onboarding.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.onboarding.adapters.outbound.MongoAdapter;
import io.angularpay.onboarding.adapters.outbound.RedisAdapter;
import io.angularpay.onboarding.domain.OnboardingRequest;
import io.angularpay.onboarding.domain.Role;
import io.angularpay.onboarding.exceptions.CommandException;
import io.angularpay.onboarding.exceptions.ErrorObject;
import io.angularpay.onboarding.helpers.CommandHelper;
import io.angularpay.onboarding.models.AssociatedReferenceResponse;
import io.angularpay.onboarding.models.UpdateEmailCommandRequest;
import io.angularpay.onboarding.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.angularpay.onboarding.exceptions.ErrorCode.ILLEGAL_REQUEST_ERROR;
import static io.angularpay.onboarding.helpers.CommandHelper.getRequestByReferenceOrThrow;

@Service
public class UpdateEmailCommand extends AbstractCommand<UpdateEmailCommandRequest, AssociatedReferenceResponse>
        implements UpdatesPublisherCommand<AssociatedReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public UpdateEmailCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter) {
        super("UpdateEmailCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(UpdateEmailCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected AssociatedReferenceResponse handle(UpdateEmailCommandRequest request) {
        OnboardingRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        if (!found.isSignupConfirmed()) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(ILLEGAL_REQUEST_ERROR)
                    .message(ILLEGAL_REQUEST_ERROR.getDefaultMessage())
                    .build();
        }
        found.setUsername(request.getUpdateEmailApiModel().getEmail().toLowerCase());
        found.setEmail(request.getUpdateEmailApiModel().getEmail().toLowerCase());
        OnboardingRequest response = this.mongoAdapter.updateRequest(found);
        return AssociatedReferenceResponse.builder().requestReference(response.getReference()).build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateEmailCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public String convertToUpdatesMessage(OnboardingRequest smartSaveRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(smartSaveRequest);
    }

    @Override
    public OnboardingRequest getByReference(AssociatedReferenceResponse associatedReferenceResponse) {
        return getRequestByReferenceOrThrow(this.mongoAdapter, associatedReferenceResponse.getRequestReference());
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }
}
