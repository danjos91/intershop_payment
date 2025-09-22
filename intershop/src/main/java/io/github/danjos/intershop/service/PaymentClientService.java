package io.github.danjos.intershop.service;

import org.springframework.beans.factory.annotation.Value;
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
    
    private final OAuth2ClientService oauth2ClientService;
    private final UserService userService;
    
    @Value("${payment.service.url}")
    private String paymentServiceUrl;
    
    public Mono<Double> getBalance() {
        // For now, return a default balance since we don't have user context here
        // This method is called from cart controller which has user context
        return Mono.just(1000.0);
    }
    
    public Mono<Double> getBalanceForUser(String username) {
        return userService.findByUsername(username)
                .map(user -> user.getBalance().doubleValue())
                .onErrorResume(e -> {
                    log.error("Error getting balance for user {}: {}", username, e.getMessage());
                    return Mono.just(1000.0); // Default balance
                });
    }
    
    public Mono<Boolean> processPayment(Double amount, String orderId) {
        Map<String, Object> request = Map.of(
            "amount", amount,
            "orderId", orderId,
            "description", "Payment for order " + orderId
        );
        
        return oauth2ClientService.executePostWithToken("/api/payment/process", request, Map.class)
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
