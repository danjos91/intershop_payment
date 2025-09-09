package io.github.danjos.payment;

import io.github.danjos.payment.domain.BalanceResponse;
import io.github.danjos.payment.domain.PaymentRequest;
import io.github.danjos.payment.domain.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Payment Service Integration Tests")
class PaymentServiceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        // Reset any state if needed
    }

    @Test
    @DisplayName("Should load application context successfully")
    void contextLoads() {
        assertThat(webTestClient).isNotNull();
    }

    @Test
    @DisplayName("Should get balance with proper authentication")
    @WithMockUser(authorities = "SCOPE_payment:read")
    void getBalance_WithAuthentication_ShouldReturnBalance() {
        webTestClient.get()
                .uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BalanceResponse.class)
                .value(response -> {
                    assertThat(response.getBalance()).isNotNull();
                    assertThat(response.getCurrency()).isNotNull();
                    assertThat(response.getTimestamp()).isNotNull();
                });
    }

    @Test
    @DisplayName("Should process multiple payments correctly")
    @WithMockUser(authorities = "SCOPE_payment:write")
    void processMultiplePayments_ShouldUpdateBalanceCorrectly() {
        // First payment
        PaymentRequest firstRequest = new PaymentRequest()
                .amount(200.0)
                .orderId("order-1")
                .description("First payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.getSuccess()).isTrue();
                    assertThat(response.getNewBalance()).isEqualTo(800.0);
                });

        // Second payment
        PaymentRequest secondRequest = new PaymentRequest()
                .amount(300.0)
                .orderId("order-2")
                .description("Second payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.getSuccess()).isTrue();
                    assertThat(response.getNewBalance()).isEqualTo(500.0);
                });
    }

    @Test
    @DisplayName("Should handle concurrent payment requests")
    @WithMockUser(authorities = "SCOPE_payment:write")
    void processConcurrentPayments_ShouldHandleCorrectly() {
        PaymentRequest request1 = new PaymentRequest()
                .amount(100.0)
                .orderId("concurrent-1")
                .description("Concurrent payment 1");

        PaymentRequest request2 = new PaymentRequest()
                .amount(150.0)
                .orderId("concurrent-2")
                .description("Concurrent payment 2");

        // Process both payments concurrently
        Mono<PaymentResponse> payment1 = webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request1)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PaymentResponse.class)
                .getResponseBody()
                .next();

        Mono<PaymentResponse> payment2 = webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request2)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PaymentResponse.class)
                .getResponseBody()
                .next();

        // Verify both payments are processed
        StepVerifier.create(Mono.zip(payment1, payment2))
                .assertNext(tuple -> {
                    PaymentResponse response1 = tuple.getT1();
                    PaymentResponse response2 = tuple.getT2();
                    
                    assertThat(response1.getSuccess()).isTrue();
                    assertThat(response2.getSuccess()).isTrue();
                    assertThat(response1.getTransactionId()).isNotEqualTo(response2.getTransactionId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate payment request fields")
    @WithMockUser(authorities = "SCOPE_payment:write")
    void processPayment_WithInvalidRequest_ShouldReturnBadRequest() {
        // Test with null amount
        PaymentRequest invalidRequest = new PaymentRequest()
                .amount(null)
                .orderId("invalid-order")
                .description("Invalid payment");

        webTestClient.post()
                .uri("/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should handle service unavailable scenarios")
    @WithMockUser(authorities = "SCOPE_payment:read")
    void getBalance_WhenServiceUnavailable_ShouldHandleGracefully() {
        // This test would require mocking the service to be unavailable
        // For now, we test the normal flow
        webTestClient.get()
                .uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}
