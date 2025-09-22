package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserBalanceController {
    
    private final UserService userService;
    
    @GetMapping("/{username}/balance")
    public Mono<ResponseEntity<BigDecimal>> getUserBalance(@PathVariable String username) {
        log.info("Getting balance for user: {}", username);
        
        return userService.findByUsername(username)
                .map(user -> {
                    BigDecimal balance = user.getBalance() != null ? user.getBalance() : BigDecimal.valueOf(1000.00);
                    log.info("User {} balance: {}", username, balance);
                    return ResponseEntity.ok(balance);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(e -> {
                    log.error("Error getting balance for user {}: {}", username, e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    @PutMapping("/{username}/balance")
    public Mono<ResponseEntity<Boolean>> updateUserBalance(@PathVariable String username, @RequestBody BigDecimal newBalance) {
        log.info("Updating balance for user {} to {}", username, newBalance);
        
        return userService.findByUsername(username)
                .flatMap(user -> {
                    user.setBalance(newBalance);
                    return userService.updateUser(user);
                })
                .map(updatedUser -> {
                    log.info("Successfully updated balance for user {} to {}", username, newBalance);
                    return ResponseEntity.ok(true);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(e -> {
                    log.error("Error updating balance for user {}: {}", username, e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
