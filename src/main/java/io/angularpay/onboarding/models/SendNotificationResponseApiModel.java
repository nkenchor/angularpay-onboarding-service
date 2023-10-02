
package io.angularpay.onboarding.models;

import lombok.Data;

@Data
public class SendNotificationResponseApiModel {

    private String reference;
    private NotificationStatus status;
}
