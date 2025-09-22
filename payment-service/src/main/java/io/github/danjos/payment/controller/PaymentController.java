package io.github.danjos.payment.controller;

import io.github.danjos.payment.domain.BalanceResponse;
import io.github.danjos.payment.domain.PaymentRequest;
import io.github.danjos.payment.domain.PaymentResponse;
import io.github.danjos.payment.service.UserBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    @Value("${payment.initial.balance:1000.00}")
    private Double initialBalance;

    @Value("${payment.currency:RUB}")
    private String currency;
    
    private final UserBalanceService userBalanceService;

    public PaymentController(UserBalanceService userBalanceService) {
        this.userBalanceService = userBalanceService;
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('SCOPE_payment:read')")
    public Mono<ResponseEntity<BalanceResponse>> getBalance(Authentication authentication) {
        String username = getUsernameFromAuth(authentication);
        
        return userBalanceService.getUserBalance(username)
                .map(balance -> {
                    log.info("Getting balance for user {}: {}", username, balance);
                    
                    BalanceResponse response = new BalanceResponse()
                            .balance(balance.doubleValue())
                            .currency(currency)
                            .timestamp(OffsetDateTime.now());
                    
                    return ResponseEntity.ok(response);
                });
    }

    @PostMapping("/process")
    @PreAuthorize("hasAuthority('SCOPE_payment:write')")
    public Mono<ResponseEntity<PaymentResponse>> processPayment(@RequestBody PaymentRequest request, Authentication authentication) {
        String username = getUsernameFromAuth(authentication);
        Double amount = request.getAmount();
        
        log.info("Processing payment for user {}: amount={}, orderId={}", username, amount, request.getOrderId());
        
        return userBalanceService.getUserBalance(username)
                .flatMap(currentBalance -> {
                    if (currentBalance.doubleValue() < amount) {
                        log.warn("Insufficient funds for user {}: required={}, available={}", username, amount, currentBalance);
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    
                    // Process payment
                    Double newBalance = currentBalance.doubleValue() - amount;
                    String transactionId = "txn-" + UUID.randomUUID().toString().substring(0, 8);
                    
                    return userBalanceService.updateUserBalance(username, java.math.BigDecimal.valueOf(newBalance))
                            .map(success -> {
                                if (success) {
                                    log.info("Payment processed successfully for user {}: transactionId={}, newBalance={}", username, transactionId, newBalance);
                                    
                                    PaymentResponse response = new PaymentResponse()
                                            .success(true)
                                            .transactionId(transactionId)
                                            .newBalance(newBalance)
                                            .message("Платеж успешно обработан")
                                            .timestamp(OffsetDateTime.now());
                                    
                                    return ResponseEntity.ok(response);
                                } else {
                                    log.error("Failed to update balance for user {}", username);
                                    return ResponseEntity.internalServerError().build();
                                }
                            });
                });
    }
    
    private String getUsernameFromAuth(Authentication authentication) {
        if (authentication == null) {
            return "anonymous";
        }
        return authentication.getName();
    }
}
