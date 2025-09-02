package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.dto.CartItemDto;
import io.github.danjos.intershop.model.Item;
import io.github.danjos.intershop.service.CartService;
import io.github.danjos.intershop.service.ItemService;
import io.github.danjos.intershop.util.Paging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import io.github.danjos.intershop.service.UserService;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping({"/", "/main"})
public class WebController {
    private final ItemService itemService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping({"/", "/items"})
    public Mono<Rendering> showMainPage(
            @RequestParam(required = false, defaultValue = "NO") String sort,
            @RequestParam(name = "search", required = false, defaultValue = "") String search,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNumber,
            Authentication authentication) {

        // Получаем ID пользователя или null для анонимных
        Mono<List<CartItemDto>> userCartMono;
        if (authentication != null && !"anonymousUser".equals(authentication.getName())) {
            userCartMono = userService.getUserIdByUsername(authentication.getName())
                .flatMap(userId -> cartService.getCartItemsReactive(userId))
                .onErrorReturn(List.of());
        } else {
            userCartMono = Mono.just(List.<CartItemDto>of());
        }

        return Mono.zip(
                itemService.searchItems(search, pageNumber, pageSize, sort),
                userCartMono
            )
            .map(tuple -> {
                Page<Item> mainPage = tuple.getT1();
                List<CartItemDto> userCart = tuple.getT2();
                
                Paging paging = new Paging(pageNumber, pageSize, mainPage.hasNext(), mainPage.hasPrevious());
                
                // Создаем Map для быстрого поиска количества товаров в корзине
                Map<Long, Integer> cartMap = userCart.stream()
                    .collect(Collectors.toMap(CartItemDto::getId, CartItemDto::getCount));
                
                List<CartItemDto> itemsWithCount = mainPage.getContent().stream()
                        .map(item -> new CartItemDto(item, cartMap.getOrDefault(item.getId(), 0)))
                        .collect(Collectors.toList());

                return Rendering.view("main")
                        .modelAttribute("items", itemsWithCount)
                        .modelAttribute("search", search)
                        .modelAttribute("paging", paging)
                        .modelAttribute("isLoggedIn", authentication != null && !"anonymousUser".equals(authentication.getName()))
                        .build();
            })
            .onErrorResume(e -> {
                log.error("Error in showMainPage", e);
                return Mono.just(Rendering.redirectTo("/error").build());
            });
    }

    @GetMapping("/main/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> handleMainItemAction(
            @PathVariable Long id,
            @RequestParam String action,
            Authentication authentication) {

        log.info("Handling main item action: {} for item: {}", action, id);
        
        // Проверяем, что пользователь авторизован
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return Mono.just(Rendering.redirectTo("/login").build());
        }
        
        // Получаем ID пользователя и выполняем операцию с корзиной
        return userService.getUserIdByUsername(authentication.getName())
            .flatMap(userId -> {
                Mono<Void> cartOperation = Mono.empty();
                
                if ("plus".equals(action)) {
                    cartOperation = cartService.addItemToCart(id, userId);
                } else if ("minus".equals(action)) {
                    cartOperation = cartService.removeItemFromCart(id, userId);
                }
                
                return cartOperation;
            })
            .then(Mono.just(Rendering.redirectTo("/").build()))
            .onErrorResume(e -> {
                log.error("Error in handleMainItemAction", e);
                return Mono.just(Rendering.redirectTo("/").build());
            });
    }
} 