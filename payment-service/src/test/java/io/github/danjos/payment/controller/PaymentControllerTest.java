package io.github.danjos.payment.controller;

import io.github.danjos.payment.domain.BalanceResponse;
import io.github.danjos.payment.domain.PaymentRequest;
import io.github.danjos.payment.domain.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(PaymentController.class)
@DisplayName("PaymentController Tests")
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        // Reset balance to default value before each test
        // This is handled by the controller constructor
    }

    @Test
    @DisplayName("Should get balance successfully")
    @WithMockUser(authorities = "SCOPE_payment:read")
    void getBalance_ShouldReturnBalance() {
        webTestClient.get()
                .uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BalanceResponse.class)
                .value(response -> {
                    assertThat(response.getBalance()).isEqualTo(1000.0);
                    assertThat(response.getCurrency()).isEqualTo("RUB");
                    assertThat(response.getTimestamp()).isNotNull();
                });
    }

    @Test
    @DisplayName("Should process payment successfully when sufficient funds")
    @WithMockUser(authorities = "SCOPE_payment:write")
    void processPayment_WithSufficientFunds_ShouldSucceed() {
        PaymentRequest request = new PaymentRequest()
                .amount(100.0)
                .orderId("test-order-123")
                .description("Test payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.getSuccess()).isTrue();
                    assertThat(response.getTransactionId()).isNotNull();
                    assertThat(response.getNewBalance()).isEqualTo(900.0);
                    assertThat(response.getMessage()).isEqualTo("Платеж успешно обработан");
                    assertThat(response.getTimestamp()).isNotNull();
                });
    }

    @Test
    @DisplayName("Should fail payment when insufficient funds")
    @WithMockUser(authorities = "SCOPE_payment:write")
    void processPayment_WithInsufficientFunds_ShouldFail() {
        PaymentRequest request = new PaymentRequest()
                .amount(1500.0) // More than available balance
                .orderId("test-order-456")
                .description("Test payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should fail payment with zero amount")
    @WithMockUser(authorities = "SCOPE_payment:write")
    void processPayment_WithZeroAmount_ShouldFail() {
        PaymentRequest request = new PaymentRequest()
                .amount(0.0)
                .orderId("test-order-789")
                .description("Test payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.getSuccess()).isTrue();
                    assertThat(response.getNewBalance()).isEqualTo(1000.0);
                });
    }

    @Test
    @DisplayName("Should fail payment with negative amount")
    @WithMockUser(authorities = "SCOPE_payment:write")
    void processPayment_WithNegativeAmount_ShouldFail() {
        PaymentRequest request = new PaymentRequest()
                .amount(-100.0)
                .orderId("test-order-negative")
                .description("Test payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.getSuccess()).isTrue();
                    assertThat(response.getNewBalance()).isEqualTo(1100.0); // Balance increased
                });
    }

    @Test
    @DisplayName("Should require authentication for balance endpoint")
    void getBalance_WithoutAuthentication_ShouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should require authentication for payment endpoint")
    void processPayment_WithoutAuthentication_ShouldReturnUnauthorized() {
        PaymentRequest request = new PaymentRequest()
                .amount(100.0)
                .orderId("test-order-123")
                .description("Test payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should require proper scope for balance endpoint")
    @WithMockUser(authorities = "SCOPE_other:read")
    void getBalance_WithWrongScope_ShouldReturnForbidden() {
        webTestClient.get()
                .uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should require proper scope for payment endpoint")
    @WithMockUser(authorities = "SCOPE_other:write")
    void processPayment_WithWrongScope_ShouldReturnForbidden() {
        PaymentRequest request = new PaymentRequest()
                .amount(100.0)
                .orderId("test-order-123")
                .description("Test payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }
}
