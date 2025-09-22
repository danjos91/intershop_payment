package io.github.danjos.intershop.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentClientService {
    
    private final UserService userService;
    
    public Mono<Double> getBalance() {
        // For now, return a default balance since we don't have user context here
        // This method is called from cart controller which has user context
        return Mono.just(1000.0);
    }
    
    public Mono<Double> getBalanceForUser(String username) {
        log.debug("Getting balance for user: {}", username);
        return userService.findByUsername(username)
                .map(user -> {
                    Double balance = user.getBalance().doubleValue();
                    log.debug("User {} has balance: {}", username, balance);
                    return balance;
                })
                .onErrorResume(e -> {
                    log.error("Error getting balance for user {}: {}", username, e.getMessage(), e);
                    return Mono.just(1000.0); // Default balance
                });
    }
    
    public Mono<Boolean> processPayment(Double amount, String orderId) {
        // For now, just return true since we're handling balance validation in the controller
        // In a real application, this would process the actual payment
        log.info("Processing payment locally: amount={}, orderId={}", amount, orderId);
        return Mono.just(true);
    }
    
    public Mono<Boolean> processPaymentForUser(String username, Double amount, String orderId) {
        log.info("Processing payment for user {}: amount={}, orderId={}", username, amount, orderId);
        return userService.findByUsername(username)
            .flatMap(user -> {
                Double currentBalance = user.getBalance().doubleValue();
                log.debug("User {} current balance: {}", username, currentBalance);
                
                if (currentBalance >= amount) {
                    // Deduct the amount from user's balance
                    Double newBalance = currentBalance - amount;
                    user.setBalance(java.math.BigDecimal.valueOf(newBalance));
                    return userService.updateUser(user)
                        .map(updatedUser -> {
                            log.info("Payment processed successfully for user {}: amount={}, newBalance={}", 
                                username, amount, newBalance);
                            return true;
                        })
                        .onErrorResume(e -> {
                            log.error("Error updating user balance after payment for user {}: {}", username, e.getMessage(), e);
                            return Mono.just(false);
                        });
                } else {
                    log.warn("Insufficient balance for user {}: required={}, available={}", 
                        username, amount, currentBalance);
                    return Mono.just(false);
                }
            })
            .onErrorResume(e -> {
                log.error("Error processing payment for user {}: {}", username, e.getMessage(), e);
                return Mono.just(false);
            });
    }
}
