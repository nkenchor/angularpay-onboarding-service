package io.angularpay.onboarding.ports.outbound;

import io.angularpay.onboarding.models.SendNotificationRequestApiModel;
import io.angularpay.onboarding.models.SendNotificationResponseApiModel;

import java.util.Map;
import java.util.Optional;

public interface NotificationServicePort {
    Optional<SendNotificationResponseApiModel> sendNotification(
            SendNotificationRequestApiModel createOtpRequestApiModel,
            Map<String, String> headers);
}
