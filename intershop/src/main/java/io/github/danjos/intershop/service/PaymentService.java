package io.github.danjos.intershop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentService {

    private final WebClient paymentServiceWebClient;
    private final OAuth2TokenService tokenService;

    @Autowired
    public PaymentService(WebClient paymentServiceWebClient, OAuth2TokenService tokenService) {
        this.paymentServiceWebClient = paymentServiceWebClient;
        this.tokenService = tokenService;
    }

    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> getBalance() {
        return tokenService.getAccessToken()
                .flatMap(token -> paymentServiceWebClient
                        .get()
                        .uri("/api/payment/balance")
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(map -> (Map<String, Object>) map))
                .onErrorResume(e -> {
                    System.err.println("Error getting balance: " + e.getMessage());
                    return Mono.just(Map.of("error", "Failed to get balance"));
                });
    }

    @SuppressWarnings("unchecked")
    public Mono<Map<String, Object>> processPayment(BigDecimal amount) {
        return tokenService.getAccessToken()
                .flatMap(token -> paymentServiceWebClient
                        .post()
                        .uri("/api/payment/process")
                        .header("Authorization", "Bearer " + token)
                        .bodyValue(Map.of("amount", amount))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(map -> (Map<String, Object>) map))
                .onErrorResume(e -> {
                    System.err.println("Error processing payment: " + e.getMessage());
                    return Mono.just(Map.of("status", "error", "message", "Payment failed"));
                });
    }
}
