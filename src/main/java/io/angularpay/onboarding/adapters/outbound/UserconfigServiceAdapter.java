package io.angularpay.onboarding.adapters.outbound;

import io.angularpay.onboarding.configurations.AngularPayConfiguration;
import io.angularpay.onboarding.exceptions.CommandException;
import io.angularpay.onboarding.models.*;
import io.angularpay.onboarding.ports.outbound.UserconfigServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import static io.angularpay.onboarding.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserconfigServiceAdapter implements UserconfigServicePort {

    private final WebClient webClient;
    private final AngularPayConfiguration configuration;

    @Override
    public ResourceCreationResponse createUserConfiguration(
            CreateUserConfigurationRequestApiModel createUserConfigurationRequestApiModel,
            Map<String, String> headers) {
        try {
            URI userconfigUrl = UriComponentsBuilder.fromUriString(configuration.getUserconfigUrl())
                    .path("/user-configuration/accounts")
                    .build().toUri();

            CreateUserConfigurationResponseApiModel createUserConfigurationResponseApiModel = webClient
                    .post()
                    .uri(userconfigUrl.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-angularpay-username", createUserConfigurationRequestApiModel.getUserProfile().getUsername())
                    .header("x-angularpay-device-id", createUserConfigurationRequestApiModel.getDevice().getDeviceId())
                    .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                    .header("x-angularpay-user-reference", "ONBOARDING-" + createUserConfigurationRequestApiModel.getOnboardingReference())
                    .header("x-angularpay-user-roles", headers.get("x-angularpay-user-roles"))
                    .body(Mono.just(createUserConfigurationRequestApiModel), CreateUserConfigurationRequestApiModel.class)
                    .exchangeToMono(response -> {
                        if (response.statusCode().equals(HttpStatus.CREATED)) {
                            return response.bodyToMono(CreateUserConfigurationResponseApiModel.class);
                        }
                        if (response.statusCode().equals(HttpStatus.CONFLICT)) {
                            throw CommandException.builder()
                                    .status(HttpStatus.CONFLICT)
                                    .errorCode(DUPLICATE_REQUEST_ERROR)
                                    .message(DUPLICATE_REQUEST_ERROR.getDefaultMessage())
                                    .build();
                        } else {
                            return Mono.empty();
                        }
                    })
                    .block();
            if (Objects.isNull(createUserConfigurationResponseApiModel)) {
                return ResourceCreationResponse.builder().success(false).errorCode(GENERIC_ERROR).build();
            } else {
                return ResourceCreationResponse.builder().success(true).reference(createUserConfigurationResponseApiModel.getReference()).build();
            }
        } catch (ResponseStatusException exception) {
            return ResourceCreationResponse.builder().success(false).errorCode(DUPLICATE_REQUEST_ERROR).build();
        }
    }

    @Override
    public ResourceDeletionResponse rollbackUserConfiguration(
            RollbackUserConfigurationRequestApiModel rollbackUserConfigurationRequestApiModel,
            Map<String, String> headers) {
        try {
            URI userconfigUrl = UriComponentsBuilder.fromUriString(configuration.getUserconfigUrl())
                    .path("/user-configuration/accounts/")
                    .path(rollbackUserConfigurationRequestApiModel.getUserReference())
                    .build().toUri();

            webClient
                    .delete()
                    .uri(userconfigUrl.toString())
                    .header("x-angularpay-username", rollbackUserConfigurationRequestApiModel.getUsername())
                    .header("x-angularpay-device-id", rollbackUserConfigurationRequestApiModel.getDeviceId())
                    .header("x-angularpay-correlation-id", headers.get("x-angularpay-correlation-id"))
                    .header("x-angularpay-user-reference", "ONBOARDING-" + rollbackUserConfigurationRequestApiModel.getUserReference())
                    .exchangeToMono(response -> {
                        if (!response.statusCode().equals(HttpStatus.OK)) {
                            throw CommandException.builder()
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .errorCode(USERCONFIG_SERVICE_ERROR)
                                    .message(USERCONFIG_SERVICE_ERROR.getDefaultMessage())
                                    .build();
                        }
                        return Mono.empty();
                    })
                    .block();
            return ResourceDeletionResponse.builder().success(true).build();
        } catch (ResponseStatusException exception) {
            return ResourceDeletionResponse.builder().success(false).errorCode(DUPLICATE_REQUEST_ERROR).build();
        }
    }
}
