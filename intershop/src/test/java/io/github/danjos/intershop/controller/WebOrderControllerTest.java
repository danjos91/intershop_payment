package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.model.Order;
import io.github.danjos.intershop.model.User;
import io.github.danjos.intershop.service.OrderService;
import io.github.danjos.intershop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@WebFluxTest(WebOrderController.class)
@DisplayName("WebOrderController Tests")
class WebOrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserService userService;

    private User user;
    private Order order1;
    private Order order2;
    private List<Order> orders;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        order1 = new Order();
        order1.setId(1L);
        order1.setUserId(user.getId());
        order1.setStatus("PROCESSING");

        order2 = new Order();
        order2.setId(2L);
        order2.setUserId(user.getId());
        order2.setStatus("COMPLETED");

        orders = Arrays.asList(order1, order2);
    }

    @Nested
    @DisplayName("Show Orders Tests")
    class ShowOrdersTests {

        @Test
        @DisplayName("Should return orders page with user orders")
        @WithMockUser(username = "testuser")
        void showOrders_WithUserOrders_ShouldReturnOrdersPage() {
            when(userService.findByUsername("testuser")).thenReturn(Mono.just(user));
            when(orderService.getUserOrders(user)).thenReturn(Flux.fromIterable(orders));

            webTestClient.get()
                    .uri("/orders")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .consumeWith(response -> {
                        String body = new String(response.getResponseBody());
                        assert body.contains("orders");
                    });
        }

        @Test
        @DisplayName("Should return orders page with empty orders")
        @WithMockUser(username = "testuser")
        void showOrders_WithEmptyOrders_ShouldReturnOrdersPage() {
            when(userService.findByUsername("testuser")).thenReturn(Mono.just(user));
            when(orderService.getUserOrders(user)).thenReturn(Flux.empty());

            webTestClient.get()
                    .uri("/orders")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("Show Order Tests")
    class ShowOrderTests {

        @Test
        @DisplayName("Should return order page with valid ID")
        @WithMockUser(username = "testuser")
        void showOrder_WithValidId_ShouldReturnOrderPage() {
            when(orderService.getOrderById(1L)).thenReturn(Mono.just(order1));

            webTestClient.get()
                    .uri("/orders/1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .consumeWith(response -> {
                        String body = new String(response.getResponseBody());
                        assert body.contains("order");
                    });
        }

        @Test
        @DisplayName("Should return order page with new order parameter")
        @WithMockUser(username = "testuser")
        void showOrder_WithNewOrderParameter_ShouldReturnOrderPage() {
            when(orderService.getOrderById(1L)).thenReturn(Mono.just(order1));

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/orders/1")
                            .queryParam("newOrder", "true")
                            .build())
                    .exchange()
                    .expectStatus().isOk();
        }
    }
} 