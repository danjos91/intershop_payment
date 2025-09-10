package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.dto.CartItemDto;
import io.github.danjos.intershop.model.Item;
import io.github.danjos.intershop.service.CartService;
import io.github.danjos.intershop.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(WebItemController.class)
@DisplayName("WebItemController Tests")
class WebItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    private Item laptop;
    private Map<Long, Integer> cart;

    @BeforeEach
    void setUp() {
        laptop = new Item();
        laptop.setId(1L);
        laptop.setTitle("Laptop");
        laptop.setDescription("High performance laptop");
        laptop.setPrice(999.99);
        laptop.setStock(10);

        cart = new HashMap<>();
        cart.put(1L, 2);
    }

    @Nested
    @DisplayName("Show Item Tests")
    class ShowItemTests {

        @Test
        @DisplayName("Should return item page with valid ID")
        void showItem_WithValidId_ShouldReturnItemPage() {
            when(itemService.getItemById(1L)).thenReturn(Mono.just(laptop));
            when(cartService.getCart(any())).thenReturn(cart);

            webTestClient.get()
                    .uri("/items/1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .consumeWith(response -> {
                        String body = new String(response.getResponseBody());
                        assert body.contains("item");
                    });
        }

        @Test
        @DisplayName("Should handle item not found gracefully")
        void showItem_WithInvalidId_ShouldHandleGracefully() {
            when(itemService.getItemById(999L))
                    .thenReturn(Mono.error(new RuntimeException("Item not found")));

            webTestClient.get()
                    .uri("/items/999")
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }
    }

    @Nested
    @DisplayName("Handle Item Action Tests")
    class HandleItemActionTests {

        @Test
        @DisplayName("Should handle valid plus action")
        void handleItemAction_WithPlusAction_ShouldSucceed() {
            when(cartService.addItemToCartReactive(eq(1L), any())).thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/items/order/1")
                            .queryParam("action", "plus")
                            .build())
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }

        @Test
        @DisplayName("Should handle valid minus action")
        void handleItemAction_WithMinusAction_ShouldSucceed() {
            when(cartService.removeItemFromCartReactive(eq(1L), any())).thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/items/order/1")
                            .queryParam("action", "minus")
                            .build())
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }
    }
} 