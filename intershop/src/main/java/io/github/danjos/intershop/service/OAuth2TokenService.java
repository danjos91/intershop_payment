package io.github.danjos.intershop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class OAuth2TokenService {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUri;
    private final String scope;

    public OAuth2TokenService(WebClient.Builder webClientBuilder,
                             @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId,
                             @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}") String clientSecret,
                             @Value("${spring.security.oauth2.client.provider.keycloak.token-uri:http://localhost:8082/realms/intershop/protocol/openid-connect/token}") String tokenUri,
                             @Value("${spring.security.oauth2.client.registration.keycloak.scope}") String scope) {
        this.webClient = webClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
        this.scope = scope;
    }

    public Mono<String> getAccessToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("scope", scope);

        return webClient.post()
                .uri(tokenUri)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> {
                    System.err.println("Error getting access token: " + e.getMessage());
                    return Mono.empty();
                });
    }
}
