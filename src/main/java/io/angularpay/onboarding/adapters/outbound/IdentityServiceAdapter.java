package io.angularpay.onboarding.adapters.outbound;

import io.angularpay.onboarding.configurations.AngularPayConfiguration;
import io.angularpay.onboarding.models.CreateUserLoginRequestApiModel;
import io.angularpay.onboarding.models.CreateUserLoginResponseApiModel;
import io.angularpay.onboarding.models.ResourceCreationResponse;
import io.angularpay.onboarding.ports.outbound.IdentityServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import static io.angularpay.onboarding.exceptions.ErrorCode.DUPLICATE_REQUEST_ERROR;
import static io.angularpay.onboarding.exceptions.ErrorCode.GENERIC_ERROR;

@Service
@RequiredArgsConstructor
public class IdentityServiceAdapter implements IdentityServicePort {

    private final WebClient webClient;
    private final AngularPayConfiguration configuration;

    @Override
    public ResourceCreationResponse createLoginAccount(
            CreateUserLoginRequestApiModel createUserLoginRequestApiModel,
            Map<String, String> headers) {
        try {
            URI identityUrl = UriComponentsBuilder.fromUriString(configuration.getIdentityUrl())
                    .path("/identity/users/onboard")
                    .build().toUri();

            CreateUserLoginResponseApiModel createUserLoginResponseApiModel = webClient
                    .post()
                    .uri(identityUrl.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-angularpay-username", createUserLoginRequestApiModel.getUsername())
                    .header("x-angularpay-device-id", createUserLoginRequestApiModel.getDevice())
                    .header("x-angularpay-user-reference", createUserLoginRequestApiModel.getUserReference())
                    .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                    .header("x-angularpay-user-roles", headers.get("x-angularpay-user-roles"))
                    .body(Mono.just(createUserLoginRequestApiModel), CreateUserLoginRequestApiModel.class)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(CreateUserLoginResponseApiModel.class);
                        } else {
                            return Mono.empty();
                        }
                    })
                    .block();
            if (Objects.isNull(createUserLoginResponseApiModel)) {
                return ResourceCreationResponse.builder().success(false).errorCode(GENERIC_ERROR).build();
            } else {
                return ResourceCreationResponse.builder().success(true).reference(createUserLoginResponseApiModel.getReference()).build();
            }
        } catch (ResponseStatusException exception) {
            return ResourceCreationResponse.builder().success(false).errorCode(DUPLICATE_REQUEST_ERROR).build();
        }
    }
}
