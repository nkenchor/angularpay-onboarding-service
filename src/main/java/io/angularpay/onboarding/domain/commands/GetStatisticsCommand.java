package io.angularpay.onboarding.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.onboarding.adapters.outbound.MongoAdapter;
import io.angularpay.onboarding.domain.Role;
import io.angularpay.onboarding.exceptions.ErrorObject;
import io.angularpay.onboarding.models.GetStatisticsCommandRequest;
import io.angularpay.onboarding.models.Statistics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GetStatisticsCommand extends AbstractCommand<GetStatisticsCommandRequest, List<Statistics>> {

    private final MongoAdapter mongoAdapter;

    public GetStatisticsCommand(ObjectMapper mapper, MongoAdapter mongoAdapter) {
        super("GetStatisticsCommand", mapper);
        this.mongoAdapter = mongoAdapter;
    }

    @Override
    protected String getResourceOwner(GetStatisticsCommandRequest request) {
        return ""; // TODO do this for non-user resources
    }

    @Override
    protected List<Statistics> handle(GetStatisticsCommandRequest request) {
        List<Statistics> statistics = new ArrayList<>();

        long total = this.mongoAdapter.getTotalCount();
        statistics.add(Statistics.builder()
                .name("Total")
                .value(String.valueOf(total))
                .build());

        long confirmedUsers = this.mongoAdapter.getCountByConfirmationStatus(true);
        statistics.add(Statistics.builder()
                .name("Confirmed OTP")
                .value(String.valueOf(confirmedUsers))
                .build());

        long unconfirmedUsers = this.mongoAdapter.getCountByConfirmationStatus(false);
        statistics.add(Statistics.builder()
                .name("OTP Not Confirmed")
                .value(String.valueOf(unconfirmedUsers))
                .build());

        long onboardedUsers = this.mongoAdapter.getCountByOnboardingStatus(true);
        statistics.add(Statistics.builder()
                .name("Onboarding Completed")
                .value(String.valueOf(onboardedUsers))
                .build());

        long nonOnboardedUsers = this.mongoAdapter.getCountByOnboardingStatus(false);
        statistics.add(Statistics.builder()
                .name("Onboarding Not Completed")
                .value(String.valueOf(nonOnboardedUsers))
                .build());

        return statistics;
    }

    @Override
    protected List<ErrorObject> validate(GetStatisticsCommandRequest request) {
        return Collections.emptyList();
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_PLATFORM_ADMIN, Role.ROLE_PLATFORM_USER);
    }
}
