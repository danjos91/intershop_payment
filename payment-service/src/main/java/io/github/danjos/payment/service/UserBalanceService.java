package io.github.danjos.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@Slf4j
public class UserBalanceService {
    
    private final WebClient webClient;
    private final String intershopUrl;
    
    public UserBalanceService(@Value("${intershop.url:http://localhost:8080}") String intershopUrl) {
        this.intershopUrl = intershopUrl;
        this.webClient = WebClient.builder()
                .baseUrl(intershopUrl)
                .build();
    }
    
    public Mono<BigDecimal> getUserBalance(String username) {
        log.info("Getting balance for user: {}", username);
        
        return webClient.get()
                .uri("/api/users/{username}/balance", username)
                .retrieve()
                .bodyToMono(BigDecimal.class)
                .doOnSuccess(balance -> log.info("Retrieved balance for {}: {}", username, balance))
                .doOnError(error -> log.error("Error getting balance for {}: {}", username, error.getMessage()))
                .onErrorReturn(BigDecimal.valueOf(1000.00)); // Default balance if error
    }
    
    public Mono<Boolean> updateUserBalance(String username, BigDecimal newBalance) {
        log.info("Updating balance for user {} to {}", username, newBalance);
        
        return webClient.put()
                .uri("/api/users/{username}/balance", username)
                .bodyValue(newBalance)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(success -> log.info("Updated balance for {}: {}", username, success))
                .doOnError(error -> log.error("Error updating balance for {}: {}", username, error.getMessage()))
                .onErrorReturn(false);
    }
}
