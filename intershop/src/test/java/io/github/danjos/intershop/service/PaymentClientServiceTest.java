package io.github.danjos.intershop.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentClientService Unit Tests")
class PaymentClientServiceTest {

    private WireMockServer wireMockServer;
    private PaymentClientService paymentClientService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
        
        paymentClientService = new PaymentClientService(webClient);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Should get balance successfully")
    void getBalance_ShouldReturnBalance() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": 1000.0, \"currency\": \"RUB\", \"timestamp\": \"2024-01-15T10:30:00Z\"}")));

        // When
        Mono<Double> result = paymentClientService.getBalance();

        // Then
        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertThat(balance).isEqualTo(1000.0);
                })
                .verifyComplete();

        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/payment/balance")));
    }

    @Test
    @DisplayName("Should handle balance service error gracefully")
    void getBalance_WhenServiceError_ShouldReturnZero() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal Server Error\"}")));

        // When
        Mono<Double> result = paymentClientService.getBalance();

        // Then
        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertThat(balance).isEqualTo(0.0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle balance service unavailable")
    void getBalance_WhenServiceUnavailable_ShouldReturnZero() {
        // Given - no stub, service will be unavailable

        // When
        Mono<Double> result = paymentClientService.getBalance();

        // Then
        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertThat(balance).isEqualTo(0.0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should process payment successfully")
    void processPayment_WithValidRequest_ShouldReturnTrue() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/api/payment/process"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\": true, \"transactionId\": \"txn-123\", \"newBalance\": 900.0, \"message\": \"Payment processed\", \"timestamp\": \"2024-01-15T10:30:00Z\"}")));

        // When
        Mono<Boolean> result = paymentClientService.processPayment(100.0, "order-123");

        // Then
        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isTrue();
                })
                .verifyComplete();

        wireMockServer.verify(postRequestedFor(urlEqualTo("/api/payment/process"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("Should handle payment failure due to insufficient funds")
    void processPayment_WithInsufficientFunds_ShouldReturnFalse() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/api/payment/process"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"INSUFFICIENT_FUNDS\", \"message\": \"Insufficient funds\"}")));

        // When
        Mono<Boolean> result = paymentClientService.processPayment(1500.0, "order-456");

        // Then
        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle payment service error")
    void processPayment_WhenServiceError_ShouldReturnFalse() {
        // Given
        wireMockServer.stubFor(post(urlEqualTo("/api/payment/process"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal Server Error\"}")));

        // When
        Mono<Boolean> result = paymentClientService.processPayment(100.0, "order-789");

        // Then
        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle payment service unavailable")
    void processPayment_WhenServiceUnavailable_ShouldReturnFalse() {
        // Given - no stub, service will be unavailable

        // When
        Mono<Boolean> result = paymentClientService.processPayment(100.0, "order-unavailable");

        // Then
        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle malformed JSON response")
    void getBalance_WithMalformedJson_ShouldReturnZero() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": invalid json")));

        // When
        Mono<Double> result = paymentClientService.getBalance();

        // Then
        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertThat(balance).isEqualTo(0.0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null balance in response")
    void getBalance_WithNullBalance_ShouldReturnZero() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": null, \"currency\": \"RUB\", \"timestamp\": \"2024-01-15T10:30:00Z\"}")));

        // When
        Mono<Double> result = paymentClientService.getBalance();

        // Then
        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertThat(balance).isEqualTo(0.0);
                })
                .verifyComplete();
    }
}
