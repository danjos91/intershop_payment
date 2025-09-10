package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.service.PaymentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final PaymentClientService paymentClientService;
    
    @GetMapping("/payment/balance")
    @PreAuthorize("permitAll()")
    public Mono<Map<String, Object>> testPaymentBalance() {
        log.info("Testing OAuth2 integration - getting payment balance");
        return paymentClientService.getBalance()
                .map(balance -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("balance", balance);
                    result.put("status", "success");
                    result.put("message", "OAuth2 integration working!");
                    return result;
                })
                .doOnSuccess(result -> log.info("Payment balance retrieved successfully: {}", result))
                .doOnError(error -> log.error("Error retrieving payment balance: {}", error.getMessage()));
    }
    
    @GetMapping("/payment/process")
    @PreAuthorize("permitAll()")
    public Mono<Map<String, Object>> testPaymentProcess() {
        log.info("Testing OAuth2 integration - processing payment");
        return paymentClientService.processPayment(100.0, "test-order-123")
                .map(success -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", success);
                    result.put("status", "success");
                    result.put("message", success ? "Payment processed successfully!" : "Payment failed");
                    result.put("amount", 100.0);
                    result.put("orderId", "test-order-123");
                    return result;
                })
                .doOnSuccess(result -> log.info("Payment processing test completed: {}", result))
                .doOnError(error -> log.error("Error processing payment: {}", error.getMessage()));
    }
}
