package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.dto.CartItemDto;
import io.github.danjos.intershop.model.User;
import io.github.danjos.intershop.service.CartService;
import io.github.danjos.intershop.service.OrderService;
import io.github.danjos.intershop.service.UserService;
import io.github.danjos.intershop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebCartController {
    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final PaymentService paymentService;

    @GetMapping("/cart/items")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> showCart(Authentication authentication) {
        // Проверяем, что пользователь авторизован
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return Mono.just(Rendering.redirectTo("/login").build());
        }
        
        return userService.getUserIdByUsername(authentication.getName())
            .flatMap(userId -> Mono.zip(
                cartService.getCartItemsReactive(userId),
                cartService.getCartTotalReactive(userId),
                cartService.isCheckoutEnabled(userId),
                userService.findByUsername(authentication.getName()),
                paymentService.getBalance()
            ))
            .map(tuple -> {
                List<CartItemDto> items = tuple.getT1();
                Double total = tuple.getT2();
                Boolean checkoutEnabled = tuple.getT3();
                User user = tuple.getT4();
                Map<String, Object> balanceResponse = tuple.getT5();
                
                // Извлекаем баланс из ответа или используем 0.0 по умолчанию
                Double balance = 0.0;
                if (balanceResponse != null && balanceResponse.containsKey("balance")) {
                    Object balanceObj = balanceResponse.get("balance");
                    if (balanceObj instanceof Number) {
                        balance = ((Number) balanceObj).doubleValue();
                    }
                }
                
                return Rendering.view("cart")
                        .modelAttribute("items", items)
                        .modelAttribute("total", total)
                        .modelAttribute("empty", items.isEmpty())
                        .modelAttribute("checkoutEnabled", checkoutEnabled)
                        .modelAttribute("user", user)
                        .modelAttribute("balance", balance)
                        .build();
            })
            .onErrorResume(e -> {
                log.error("Error in showCart", e);
                return Mono.just(Rendering.redirectTo("/error").build());
            });
    }

    @GetMapping("/cart/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> handleCartAction(
            @PathVariable Long id, 
            @RequestParam String action, 
            Authentication authentication) {
        
        log.info("Handling cart action: {} for item: {}", action, id);
        
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
                } else if ("delete".equals(action)) {
                    cartOperation = cartService.deleteItemFromCart(id, userId);
                }
                
                return cartOperation;
            })
            .then(Mono.just(Rendering.redirectTo("/cart/items").build()))
            .onErrorResume(e -> {
                log.error("Error in handleCartAction", e);
                return Mono.just(Rendering.redirectTo("/cart/items").build());
            });
    }

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> createOrder(Authentication authentication) {
        log.info("Creating order from cart");
        
        // Проверяем, что пользователь авторизован
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return Mono.just(Rendering.redirectTo("/login").build());
        }
        
        return userService.findByUsername(authentication.getName())
            .flatMap(user -> {
                return userService.getUserIdByUsername(authentication.getName())
                    .flatMap(userId -> Mono.zip(
                        cartService.getCartItemsReactive(userId),
                        cartService.getCartTotalReactive(userId)
                    ))
                    .flatMap(tuple -> {
                        List<CartItemDto> cartItems = tuple.getT1();
                        Double total = tuple.getT2();
                        
                        if (cartItems.isEmpty()) {
                            return Mono.just(Rendering.redirectTo("/cart/items").build());
                        }
                        
                        // Конвертируем CartItemDto в Map для OrderService
                        Map<Long, Integer> cartMap = cartItems.stream()
                            .collect(Collectors.toMap(item -> item.getId(), CartItemDto::getCount));
                        
                        // Сначала обрабатываем платеж через OAuth2
                        return paymentService.processPayment(BigDecimal.valueOf(total))
                            .flatMap(paymentResult -> {
                                if ("success".equals(paymentResult.get("status"))) {
                                    // Платеж успешен, создаем заказ
                                    return orderService.createOrderFromCart(cartMap, user)
                                        .then(cartService.clearUserCart(user.getId()))
                                        .then(Mono.just(Rendering.redirectTo("/orders?success=true").build()));
                                } else {
                                    // Платеж не прошел
                                    return Mono.just(Rendering.redirectTo("/cart/items?paymentFailed=true").build());
                                }
                            })
                            .onErrorResume(e -> {
                                log.error("Payment processing failed", e);
                                return Mono.just(Rendering.redirectTo("/cart/items?paymentFailed=true").build());
                            });
                    });
            })
            .onErrorResume(e -> {
                log.error("Error in createOrder", e);
                return Mono.just(Rendering.redirectTo("/error").build());
            });
    }
} 