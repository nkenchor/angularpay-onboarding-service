
package io.angularpay.onboarding.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("onboarding_requests")
public class OnboardingRequest {

    @Id
    private String id;
    @Version
    private int version;
    private String reference;
    @JsonProperty("created_on")
    private String createdOn;
    private String email;
    @JsonProperty("last_modified")
    private String lastModified;
    private String phone;
    @JsonProperty("signup_confirmed")
    private boolean signupConfirmed;
    private boolean userOnboarded;
    private String username;
    private Device device;
}
