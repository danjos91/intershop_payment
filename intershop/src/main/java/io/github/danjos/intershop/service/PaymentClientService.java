package io.github.danjos.intershop.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentClientService {
    
    private final WebClient webClient;
    
    public PaymentClientService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }
    
    public Mono<Double> getBalance() {
        return webClient.get()
                .uri("/api/payment/balance")
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    Object balance = response.get("balance");
                    if (balance instanceof Number) {
                        return ((Number) balance).doubleValue();
                    }
                    return 0.0;
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Error getting balance from payment service: {}", e.getMessage());
                    return Mono.just(0.0);
                })
                .onErrorResume(e -> {
                    log.error("Unexpected error getting balance: {}", e.getMessage());
                    return Mono.just(0.0);
                });
    }
    
    public Mono<Boolean> processPayment(Double amount, String orderId) {
        Map<String, Object> request = Map.of(
            "amount", amount,
            "orderId", orderId,
            "description", "Payment for order " + orderId
        );
        
        return webClient.post()
                .uri("/api/payment/process")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    Object success = response.get("success");
                    return success instanceof Boolean && (Boolean) success;
                })
                .onErrorResume(WebClientResponseException.BadRequest.class, e -> {
                    log.warn("Payment failed for order {}: {}", orderId, e.getMessage());
                    return Mono.just(false);
                })
                .onErrorResume(e -> {
                    log.error("Error processing payment for order {}: {}", orderId, e.getMessage());
                    return Mono.just(false);
                });
    }
}
