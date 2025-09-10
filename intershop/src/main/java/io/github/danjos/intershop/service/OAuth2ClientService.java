package io.github.danjos.intershop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OAuth2ClientService {
    
    private final WebClient webClient;
    private final String keycloakTokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    
    public OAuth2ClientService(
            @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuerUri,
            @Value("${spring.security.oauth2.client.registration.intershop-app.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.intershop-app.client-secret}") String clientSecret,
            @Value("${spring.security.oauth2.client.registration.intershop-app.scope}") String scope) {
        
        this.webClient = WebClient.builder()
                .baseUrl(issuerUri)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        
        this.keycloakTokenUrl = issuerUri + "/protocol/openid-connect/token";
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }
    
    /**
     * Get access token using Client Credentials flow
     */
    public Mono<String> getAccessToken() {
        return webClient.post()
                .uri("/protocol/openid-connect/token")
                .bodyValue(createTokenRequest())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .onErrorMap(throwable -> new RuntimeException("Failed to get access token", throwable));
    }
    
    /**
     * Create token request body for Client Credentials flow
     */
    private String createTokenRequest() {
        return String.format(
                "grant_type=client_credentials&client_id=%s&client_secret=%s&scope=%s",
                clientId, clientSecret, scope
        );
    }
    
    /**
     * Get WebClient with OAuth2 authorization header
     */
    public Mono<WebClient> getAuthorizedWebClient() {
        return getAccessToken()
                .map(token -> WebClient.builder()
                        .baseUrl("http://localhost:8081")
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());
    }
    
    /**
     * Execute request with OAuth2 token - reactive approach
     */
    public <T> Mono<T> executeWithToken(String uri, Class<T> responseType) {
        return getAccessToken()
                .flatMap(token -> WebClient.builder()
                        .baseUrl("http://localhost:8081")
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
                        .get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(responseType));
    }
    
    /**
     * Execute POST request with OAuth2 token - reactive approach
     */
    public <T, R> Mono<R> executePostWithToken(String uri, T requestBody, Class<R> responseType) {
        return getAccessToken()
                .flatMap(token -> WebClient.builder()
                        .baseUrl("http://localhost:8081")
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
                        .post()
                        .uri(uri)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(responseType));
    }
}
