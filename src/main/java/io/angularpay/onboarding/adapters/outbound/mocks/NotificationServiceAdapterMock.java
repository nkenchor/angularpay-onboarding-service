package io.angularpay.onboarding.adapters.outbound.mocks;

import io.angularpay.onboarding.models.NotificationStatus;
import io.angularpay.onboarding.models.SendNotificationRequestApiModel;
import io.angularpay.onboarding.models.SendNotificationResponseApiModel;
import io.angularpay.onboarding.ports.outbound.NotificationServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceAdapterMock implements NotificationServicePort {

    @Override
    public Optional<SendNotificationResponseApiModel> sendNotification(
            SendNotificationRequestApiModel sendNotificationRequestApiModel,
            Map<String, String> headers) {
        SendNotificationResponseApiModel notificationResponseApiModel = new SendNotificationResponseApiModel();
        notificationResponseApiModel.setReference(UUID.randomUUID().toString());
        notificationResponseApiModel.setStatus(NotificationStatus.SENT);
        return Optional.of(notificationResponseApiModel);
    }
}
