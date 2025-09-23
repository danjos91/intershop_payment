package io.github.danjos.intershop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PaymentClientService Integration Tests")
class PaymentClientServiceIntegrationTest {

    @Autowired
    private PaymentClientService paymentClientService;

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        @Primary
        public WebClient testWebClient() {
            return WebClient.builder()
                    .baseUrl("http://localhost:8081")
                    .build();
        }
    }

    @BeforeEach
    void setUp() {
        System.setProperty("test.mode", "true");
        assertThat(paymentClientService).isNotNull();
    }

    @Test
    @DisplayName("Should create service instance successfully")
    void shouldCreateServiceInstance() {
        assertThat(paymentClientService).isNotNull();
    }

    @Test
    @DisplayName("Should handle getBalance method call with connection error")
    void shouldHandleGetBalanceMethodCall() {
        Mono<Double> result = paymentClientService.getBalance();

        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertThat(balance).isNotNull();
                    assertThat(balance).isEqualTo(0.0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle processPayment method call with connection error")
    void shouldHandleProcessPaymentMethodCall() {
        Mono<Boolean> result = paymentClientService.processPayment(100.0, "test-order-123");

        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isNotNull();
                    assertThat(success).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle processPayment with zero amount")
    void shouldHandleProcessPaymentWithZeroAmount() {
        Mono<Boolean> result = paymentClientService.processPayment(0.0, "test-order-zero");

        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isNotNull();
                    assertThat(success).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle processPayment with large amount")
    void shouldHandleProcessPaymentWithLargeAmount() {
        Mono<Boolean> result = paymentClientService.processPayment(999999.99, "test-order-large");

        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isNotNull();
                    assertThat(success).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle processPayment with negative amount")
    void shouldHandleProcessPaymentWithNegativeAmount() {
        Mono<Boolean> result = paymentClientService.processPayment(-100.0, "test-order-negative");

        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isNotNull();
                    assertThat(success).isFalse();
                })
                .verifyComplete();
    }
}

