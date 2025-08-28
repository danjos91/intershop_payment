package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.dto.CartItemDto;
import io.github.danjos.intershop.service.CartService;
import io.github.danjos.intershop.service.ItemService;
import io.github.danjos.intershop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/items")
public class WebItemController {
    private final ItemService itemService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/{id}")
    public Mono<Rendering> showItem(@PathVariable Long id, Authentication authentication) {
        // Получаем корзину пользователя или пустую корзину для анонимных
        Mono<List<CartItemDto>> userCartMono;
        if (authentication != null && !"anonymousUser".equals(authentication.getName())) {
            userCartMono = userService.getUserIdByUsername(authentication.getName())
                .flatMap(userId -> cartService.getCartItemsReactive(userId))
                .onErrorReturn(List.of());
        } else {
            userCartMono = Mono.just(List.of());
        }

        return Mono.zip(
                itemService.getItemById(id),
                userCartMono
                )
                .map(tuple -> {
                    var item = tuple.getT1();
                    List<CartItemDto> userCart = tuple.getT2();
                    
                    // Находим количество товара в корзине пользователя
                    int count = userCart.stream()
                        .filter(cartItem -> cartItem.getId().equals(id))
                        .findFirst()
                        .map(CartItemDto::getCount)
                        .orElse(0);

                    CartItemDto itemWithCount = new CartItemDto(item, count);

                    return Rendering.view("item")
                            .modelAttribute("item", itemWithCount)
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("Error in showItem", e);
                    return Mono.just(Rendering.redirectTo("/error").build());
                });
    }

    @GetMapping("/order/{id}")
    public Mono<Rendering> handleItemAction(
            @PathVariable Long id,
            @RequestParam String action,
            Authentication authentication) {

        log.info("Handling item action: {} for item: {}", action, id);

        // Проверяем, что пользователь авторизован
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return Mono.just(Rendering.redirectTo("/login").build());
        }

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
            .then(Mono.just(Rendering.redirectTo("/items/" + id).build()))
            .onErrorResume(e -> {
                log.error("Error in handleItemAction", e);
                return Mono.just(Rendering.redirectTo("/items/" + id).build());
            });
    }
}