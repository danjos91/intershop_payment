package io.github.danjos.intershop.controller;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(WebController.class)
@DisplayName("WebController Tests")
class WebControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    private Item laptop;
    private Item smartphone;
    private Page<Item> itemPage;
    private Map<Long, Integer> cart;

    @BeforeEach
    void setUp() {
        laptop = new Item();
        laptop.setId(1L);
        laptop.setTitle("Laptop");
        laptop.setDescription("High performance laptop");
        laptop.setPrice(999.99);
        laptop.setStock(10);

        smartphone = new Item();
        smartphone.setId(2L);
        smartphone.setTitle("Smartphone");
        smartphone.setDescription("Latest smartphone");
        smartphone.setPrice(599.99);
        smartphone.setStock(15);

        itemPage = new PageImpl<>(List.of(laptop, smartphone), PageRequest.of(0, 10), 2);
        
        cart = new HashMap<>();
        cart.put(1L, 2);
        cart.put(2L, 1);
    }

    @Nested
    @DisplayName("Show Main Page Tests")
    class ShowMainPageTests {

        @Test
        @DisplayName("Should return main page with items")
        void showMainPage_WithDefaultParameters_ShouldReturnMainPage() {
            when(itemService.searchItems("", 1, 10, "NO"))
                    .thenReturn(Mono.just(itemPage));
            when(cartService.getCart(any())).thenReturn(cart);

            webTestClient.get()
                    .uri("/")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .consumeWith(response -> {
                        String body = new String(response.getResponseBody());
                        assert body.contains("main");
                    });
        }

        @Test
        @DisplayName("Should return filtered results with search query")
        void showMainPage_WithSearchQuery_ShouldReturnFilteredResults() {
            Page<Item> filteredPage = new PageImpl<>(List.of(laptop), PageRequest.of(0, 10), 1);
            when(itemService.searchItems("laptop", 1, 10, "NO"))
                    .thenReturn(Mono.just(filteredPage));
            when(cartService.getCart(any())).thenReturn(cart);

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/")
                            .queryParam("search", "laptop")
                            .build())
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Should return sorted results with sorting parameter")
        void showMainPage_WithSorting_ShouldReturnSortedResults() {
            when(itemService.searchItems("", 1, 10, "ALPHA"))
                    .thenReturn(Mono.just(itemPage));
            when(cartService.getCart(any())).thenReturn(cart);

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/")
                            .queryParam("sort", "ALPHA")
                            .build())
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Should return correct page with pagination")
        void showMainPage_WithPagination_ShouldReturnCorrectPage() {
            when(itemService.searchItems("", 2, 10, "NO"))
                    .thenReturn(Mono.just(itemPage));
            when(cartService.getCart(any())).thenReturn(cart);

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/")
                            .queryParam("pageNumber", "2")
                            .build())
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Should handle empty search results")
        void showMainPage_WithEmptySearchResults_ShouldReturnEmptyPage() {
            Page<Item> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(itemService.searchItems("nonexistent", 1, 10, "NO"))
                    .thenReturn(Mono.just(emptyPage));
            when(cartService.getCart(any())).thenReturn(new HashMap<>());

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/")
                            .queryParam("search", "nonexistent")
                            .build())
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("Handle Main Item Action Tests")
    class HandleMainItemActionTests {

        @Test
        @DisplayName("Should handle valid plus action")
        void handleMainItemAction_WithPlusAction_ShouldSucceed() {
            when(cartService.addItemToCartReactive(eq(1L), any())).thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/main/items/1")
                            .queryParam("action", "plus")
                            .build())
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }

        @Test
        @DisplayName("Should handle valid minus action")
        void handleMainItemAction_WithMinusAction_ShouldSucceed() {
            when(cartService.removeItemFromCartReactive(eq(1L), any())).thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/main/items/1")
                            .queryParam("action", "minus")
                            .build())
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }
    }
} 