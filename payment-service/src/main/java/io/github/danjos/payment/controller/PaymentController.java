package io.github.danjos.payment.controller;

import io.github.danjos.payment.domain.BalanceResponse;
import io.github.danjos.payment.domain.PaymentRequest;
import io.github.danjos.payment.domain.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private Double currentBalance;

    public PaymentController() {
        this.currentBalance = 1000.00; // Default value
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('SCOPE_payment:read')")
    public Mono<ResponseEntity<BalanceResponse>> getBalance() {
        log.info("Getting balance: {}", currentBalance);
        
        BalanceResponse response = new BalanceResponse()
                .balance(currentBalance)
                .currency(currency)
                .timestamp(OffsetDateTime.now());
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @PostMapping("/process")
    @PreAuthorize("hasAuthority('SCOPE_payment:write')")
    public Mono<ResponseEntity<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        log.info("Processing payment: amount={}, orderId={}", request.getAmount(), request.getOrderId());
        
        Double amount = request.getAmount();
        
        if (currentBalance < amount) {
            log.warn("Insufficient funds: required={}, available={}", amount, currentBalance);
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        // Process payment
        currentBalance = currentBalance - amount;
        String transactionId = "txn-" + UUID.randomUUID().toString().substring(0, 8);
        
        log.info("Payment processed successfully: transactionId={}, newBalance={}", transactionId, currentBalance);
        
        PaymentResponse response = new PaymentResponse()
                .success(true)
                .transactionId(transactionId)
                .newBalance(currentBalance)
                .message("Платеж успешно обработан")
                .timestamp(OffsetDateTime.now());
        
        return Mono.just(ResponseEntity.ok(response));
    }
}
