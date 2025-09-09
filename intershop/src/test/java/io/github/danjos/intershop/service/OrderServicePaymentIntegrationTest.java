package io.github.danjos.intershop.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.danjos.intershop.AbstractTestContainerTest;
import io.github.danjos.intershop.model.Item;
import io.github.danjos.intershop.model.Order;
import io.github.danjos.intershop.model.User;
import io.github.danjos.intershop.repository.ItemRepository;
import io.github.danjos.intershop.repository.OrderRepository;
import io.github.danjos.intershop.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OrderService Payment Integration Tests")
class OrderServicePaymentIntegrationTest extends AbstractTestContainerTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

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
    @DisplayName("Should calculate order total correctly")
    void calculateOrderTotal_ShouldReturnCorrectTotal() {
        // Given
        Item item1 = new Item();
        item1.setId(1L);
        item1.setPrice(100.0);
        item1.setTitle("Item 1");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setPrice(50.0);
        item2.setTitle("Item 2");

        Map<Long, Integer> cartItems = Map.of(
            1L, 2, // 2 * 100.0 = 200.0
            2L, 3  // 3 * 50.0 = 150.0
        );

        // When
        Mono<Double> result = orderService.calculateOrderTotal(cartItems);

        // Then
        StepVerifier.create(result)
                .assertNext(total -> {
                    assertThat(total).isEqualTo(350.0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check if payment can be processed with sufficient balance")
    void canProcessPayment_WithSufficientBalance_ShouldReturnTrue() {
        // Given
        Item item = new Item();
        item.setId(1L);
        item.setPrice(100.0);
        item.setTitle("Test Item");

        Map<Long, Integer> cartItems = Map.of(1L, 1);

        // Mock payment service to return sufficient balance
        wireMockServer.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": 500.0, \"currency\": \"RUB\", \"timestamp\": \"2024-01-15T10:30:00Z\"}")));

        // When
        Mono<Boolean> result = orderService.canProcessPayment(cartItems);

        // Then
        StepVerifier.create(result)
                .assertNext(canProcess -> {
                    assertThat(canProcess).isTrue();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check if payment can be processed with insufficient balance")
    void canProcessPayment_WithInsufficientBalance_ShouldReturnFalse() {
        // Given
        Item item = new Item();
        item.setId(1L);
        item.setPrice(1000.0);
        item.setTitle("Expensive Item");

        Map<Long, Integer> cartItems = Map.of(1L, 1);

        // Mock payment service to return insufficient balance
        wireMockServer.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": 500.0, \"currency\": \"RUB\", \"timestamp\": \"2024-01-15T10:30:00Z\"}")));

        // When
        Mono<Boolean> result = orderService.canProcessPayment(cartItems);

        // Then
        StepVerifier.create(result)
                .assertNext(canProcess -> {
                    assertThat(canProcess).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get available balance from payment service")
    void getAvailableBalance_ShouldReturnBalance() {
        // Given
        wireMockServer.stubFor(get(urlEqualTo("/api/payment/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"balance\": 750.0, \"currency\": \"RUB\", \"timestamp\": \"2024-01-15T10:30:00Z\"}")));

        // When
        Mono<Double> result = orderService.getAvailableBalance();

        // Then
        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertThat(balance).isEqualTo(750.0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle payment service unavailable when getting balance")
    void getAvailableBalance_WhenServiceUnavailable_ShouldReturnZero() {
        // Given - no stub, service will be unavailable

        // When
        Mono<Double> result = orderService.getAvailableBalance();

        // Then
        StepVerifier.create(result)
                .assertNext(balance -> {
                    assertThat(balance).isEqualTo(0.0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should process order payment successfully")
    void processOrderPayment_WithSuccessfulPayment_ShouldUpdateOrderStatus() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        Order order = new Order();
        order.setId(1L);
        order.setUserId(user.getId());
        order.setStatus("PROCESSING");

        // Mock successful payment
        wireMockServer.stubFor(post(urlEqualTo("/api/payment/process"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\": true, \"transactionId\": \"txn-123\", \"newBalance\": 900.0, \"message\": \"Payment processed\"}")));

        // When
        Mono<Boolean> result = orderService.processOrderPayment(order);

        // Then
        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isTrue();
                })
                .verifyComplete();

        // Verify the order status was updated
        StepVerifier.create(orderRepository.findById(order.getId()))
                .assertNext(savedOrder -> {
                    assertThat(savedOrder.getStatus()).isEqualTo("PAID");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle payment failure")
    void processOrderPayment_WithFailedPayment_ShouldUpdateOrderStatus() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        Order order = new Order();
        order.setId(1L);
        order.setUserId(user.getId());
        order.setStatus("PROCESSING");

        // Mock failed payment
        wireMockServer.stubFor(post(urlEqualTo("/api/payment/process"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"INSUFFICIENT_FUNDS\", \"message\": \"Insufficient funds\"}")));

        // When
        Mono<Boolean> result = orderService.processOrderPayment(order);

        // Then
        StepVerifier.create(result)
                .assertNext(success -> {
                    assertThat(success).isFalse();
                })
                .verifyComplete();

        // Verify the order status was updated
        StepVerifier.create(orderRepository.findById(order.getId()))
                .assertNext(savedOrder -> {
                    assertThat(savedOrder.getStatus()).isEqualTo("PAYMENT_FAILED");
                })
                .verifyComplete();
    }
}
