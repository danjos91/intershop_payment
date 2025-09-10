package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.dto.CartItemDto;
import io.github.danjos.intershop.model.Item;
import io.github.danjos.intershop.model.Order;
import io.github.danjos.intershop.model.User;
import io.github.danjos.intershop.service.CartService;
import io.github.danjos.intershop.service.OrderService;
import io.github.danjos.intershop.service.UserService;
import io.github.danjos.intershop.service.PaymentClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebCartController Tests")
class WebCartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private PaymentClientService paymentClientService;

    @Mock
    private WebSession webSession;

    @InjectMocks
    private WebCartController webCartController;

    private Item laptop;
    private User user;
    private Order order;
    private List<CartItemDto> cartItems;

    @BeforeEach
    void setUp() {
        laptop = new Item();
        laptop.setId(1L);
        laptop.setTitle("Laptop");
        laptop.setDescription("High performance laptop");
        laptop.setPrice(999.99);
        laptop.setStock(10);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        order = new Order();
        order.setId(1L);
        order.setUserId(user.getId());
        order.setStatus("PROCESSING");

        CartItemDto cartItem = new CartItemDto(laptop, 2);
        cartItems = Arrays.asList(cartItem);
    }

    @Nested
    @DisplayName("Handle Cart Action Tests")
    class HandleCartActionTests {

        @Test
        @DisplayName("Should handle valid plus action")
        void handleCartAction_WithPlusAction_ShouldSucceed() {
            when(cartService.addItemToCartReactive(eq(1L), any(WebSession.class))).thenReturn(Mono.empty());

            Mono<Rendering> result = webCartController.handleCartAction(1L, "plus", webSession);

            StepVerifier.create(result)
                    .expectNextMatches(rendering -> rendering != null)
                    .verifyComplete();

            verify(cartService).addItemToCartReactive(eq(1L), eq(webSession));
        }

        @Test
        @DisplayName("Should handle valid minus action")
        void handleCartAction_WithMinusAction_ShouldSucceed() {
            when(cartService.removeItemFromCartReactive(eq(1L), any(WebSession.class))).thenReturn(Mono.empty());

            Mono<Rendering> result = webCartController.handleCartAction(1L, "minus", webSession);

            StepVerifier.create(result)
                    .expectNextMatches(rendering -> rendering != null)
                    .verifyComplete();

            verify(cartService).removeItemFromCartReactive(eq(1L), eq(webSession));
        }
    }

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order with valid cart")
        void createOrder_WithValidCart_ShouldCreateOrder() {
            Map<Long, Integer> cart = new HashMap<>();
            cart.put(1L, 2);
            
            when(cartService.getCart(any(WebSession.class))).thenReturn(cart);
            when(cartService.getCartTotalReactive(any(WebSession.class))).thenReturn(Mono.just(999.99));
            when(userService.getCurrentUser()).thenReturn(Mono.just(user));
            when(paymentClientService.processPayment(anyDouble(), anyString())).thenReturn(Mono.just(true));
            when(orderService.createOrderFromCart(any(), any())).thenReturn(Mono.just(order));

            Mono<Rendering> result = webCartController.createOrder(webSession);

            StepVerifier.create(result)
                    .expectNextMatches(rendering -> rendering != null)
                    .verifyComplete();

            verify(orderService).createOrderFromCart(eq(cart), eq(user));
        }

        @Test
        @DisplayName("Should handle empty cart")
        void createOrder_WithEmptyCart_ShouldCreateOrder() {
            Map<Long, Integer> emptyCart = new HashMap<>();
            
            when(cartService.getCart(any(WebSession.class))).thenReturn(emptyCart);
            when(cartService.getCartTotalReactive(any(WebSession.class))).thenReturn(Mono.just(0.0));
            when(userService.getCurrentUser()).thenReturn(Mono.just(user));
            when(paymentClientService.processPayment(anyDouble(), anyString())).thenReturn(Mono.just(true));
            when(orderService.createOrderFromCart(any(), any())).thenReturn(Mono.just(order));

            Mono<Rendering> result = webCartController.createOrder(webSession);

            StepVerifier.create(result)
                    .expectNextMatches(rendering -> rendering != null)
                    .verifyComplete();

            // The controller processes empty carts too, so we verify it was called
            verify(orderService).createOrderFromCart(eq(emptyCart), eq(user));
        }
    }
} 