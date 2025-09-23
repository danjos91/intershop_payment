package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.dto.CartItemDto;
import io.github.danjos.intershop.model.User;
import io.github.danjos.intershop.service.CartService;
import io.github.danjos.intershop.service.OrderService;
import io.github.danjos.intershop.service.UserService;
import io.github.danjos.intershop.service.PaymentClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebCartController {
    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final PaymentClientService paymentClientService;

    @GetMapping("/cart/items")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> showCart(Authentication authentication, 
                                   @RequestParam(required = false) String error,
                                   @RequestParam(required = false) String success) {
        // Проверяем, что пользователь авторизован
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthenticated access attempt to cart for user: {}", 
                authentication != null ? authentication.getName() : "null");
            return Mono.just(Rendering.redirectTo("/login").build());
        }
        
        log.info("Accessing cart for authenticated user: {}", authentication.getName());
        
        return userService.getOrCreateUserIdByUsername(authentication.getName())
            .flatMap(userId -> {
                log.debug("Found/created user ID: {} for username: {}", userId, authentication.getName());
                return Mono.zip(
                    cartService.getCartItemsReactive(userId).onErrorReturn(List.of()),
                    cartService.getCartTotalReactive(userId).onErrorReturn(0.0),
                    cartService.isCheckoutEnabled(userId).onErrorReturn(false),
                    userService.findByUsername(authentication.getName()).onErrorReturn(createDefaultUser(authentication.getName())),
                    paymentClientService.getBalanceForUser(authentication.getName()).onErrorReturn(1000.0)
                );
            })
            .map(tuple -> {
                List<CartItemDto> items = tuple.getT1();
                Double total = tuple.getT2();
                Boolean checkoutEnabled = tuple.getT3();
                User user = tuple.getT4();
                Double balance = tuple.getT5();
                
                log.debug("Cart data - items: {}, total: {}, checkoutEnabled: {}, balance: {}", 
                    items.size(), total, checkoutEnabled, balance);
                
                return Rendering.view("cart")
                        .modelAttribute("items", items)
                        .modelAttribute("total", total)
                        .modelAttribute("empty", items.isEmpty())
                        .modelAttribute("checkoutEnabled", checkoutEnabled)
                        .modelAttribute("user", user)
                        .modelAttribute("balance", balance)
                        .modelAttribute("error", error)
                        .modelAttribute("success", success)
                        .build();
            })
            .onErrorResume(e -> {
                log.error("Error in showCart for user: {}", authentication.getName(), e);
                // Return a cart page with empty data instead of redirecting to error
                return Mono.just(Rendering.view("cart")
                        .modelAttribute("items", List.<CartItemDto>of())
                        .modelAttribute("total", 0.0)
                        .modelAttribute("empty", true)
                        .modelAttribute("checkoutEnabled", false)
                        .modelAttribute("user", createDefaultUser(authentication.getName()))
                        .modelAttribute("balance", 1000.0)
                        .modelAttribute("error", "Unable to load cart data. Please try again.")
                        .modelAttribute("success", success)
                        .build());
            });
    }

    @GetMapping("/cart/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> handleCartAction(
            @PathVariable Long id, 
            @RequestParam String action, 
            Authentication authentication) {
        
        log.info("Handling cart action: {} for item: {} by user: {}", action, id, 
            authentication != null ? authentication.getName() : "null");
        
        // Проверяем, что пользователь авторизован
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthenticated access attempt to cart action for user: {}", 
                authentication != null ? authentication.getName() : "null");
            return Mono.just(Rendering.redirectTo("/login").build());
        }
        
        return userService.getOrCreateUserIdByUsername(authentication.getName())
            .flatMap(userId -> {
                log.debug("Found/created user ID: {} for cart action by user: {}", userId, authentication.getName());
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
                log.error("Error in handleCartAction for user: {}", authentication.getName(), e);
                return Mono.just(Rendering.redirectTo("/cart/items?error=true").build());
            });
    }

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> createOrder(Authentication authentication) {
        log.info("Creating order from cart for user: {}", 
            authentication != null ? authentication.getName() : "null");
        
        // Проверяем, что пользователь авторизован
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            log.warn("Unauthenticated access attempt to create order for user: {}", 
                authentication != null ? authentication.getName() : "null");
            return Mono.just(Rendering.redirectTo("/login").build());
        }
        
        return userService.findByUsername(authentication.getName())
            .flatMap(user -> {
                return userService.getOrCreateUserIdByUsername(authentication.getName())
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
                        
                        // Process payment and create order
                        return paymentClientService.processPaymentForUser(authentication.getName(), total, "order-" + System.currentTimeMillis())
                            .flatMap(paymentSuccess -> {
                                if (!paymentSuccess) {
                                    log.error("Payment failed for user {}", authentication.getName());
                                    return Mono.just(Rendering.redirectTo("/cart/items?error=insufficient_balance").build());
                                }
                                
                                // Create order after successful payment
                                return orderService.createOrderFromCart(cartMap, user)
                                    .flatMap(order -> {
                                        // Clear the cart after successful order creation
                                        return userService.getOrCreateUserIdByUsername(authentication.getName())
                                            .flatMap(cartService::clearUserCart)
                                            .then(Mono.just(Rendering.redirectTo("/orders?success=true").build()));
                                    });
                            });
                    });
            })
            .onErrorResume(e -> {
                log.error("Error in createOrder", e);
                return Mono.just(Rendering.redirectTo("/error").build());
            });
    }
    
    private User createDefaultUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setBalance(BigDecimal.valueOf(1000.0));
        return user;
    }
} 