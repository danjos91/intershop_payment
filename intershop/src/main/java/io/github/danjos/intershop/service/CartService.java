package io.github.danjos.intershop.service;

import io.github.danjos.intershop.dto.CartItemDto;
import io.github.danjos.intershop.model.CartItem;
import io.github.danjos.intershop.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final ItemService itemService;
    private final PaymentClientService paymentClientService;
    private final CartItemRepository cartItemRepository;

    // Методы для работы с корзиной пользователя (новые)
    public Mono<Void> addItemToCart(Long itemId, Long userId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
            .switchIfEmpty(Mono.defer(() -> {
                CartItem newCartItem = new CartItem();
                newCartItem.setUserId(userId);
                newCartItem.setItemId(itemId);
                newCartItem.setQuantity(1);
                return cartItemRepository.save(newCartItem);
            }))
            .flatMap(existingItem -> {
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                return cartItemRepository.save(existingItem);
            })
            .then();
    }

    public Mono<Void> removeItemFromCart(Long itemId, Long userId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
            .flatMap(cartItem -> {
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                    return cartItemRepository.save(cartItem);
                } else {
                    return cartItemRepository.delete(cartItem);
                }
            })
            .then();
    }

    public Mono<Void> deleteItemFromCart(Long itemId, Long userId) {
        return cartItemRepository.deleteByUserIdAndItemId(userId, itemId);
    }

    public Mono<List<CartItemDto>> getCartItemsReactive(Long userId) {
        return cartItemRepository.findByUserId(userId)
                return Mono.fromRunnable(() -> {
                    Map<Long, Integer> cart = getCart(session);
                    cart.remove(itemId);
                    session.getAttributes().put("cart", cart);
                });
            }
        
            public Map<Long, Integer> getCart(WebSession session) {
                Object cartAttribute = session.getAttributes().get("cart");
                Map<Long, Integer> cart;
                if (cartAttribute instanceof Map) {
                    cart = (Map<Long, Integer>) cartAttribute;
                } else {
                    cart = new HashMap<>();
                    session.getAttributes().put("cart", cart);
                }
                return cart;
            }
        
            public Mono<List<CartItemDto>> getCartItemsReactive(WebSession session) {
                Map<Long, Integer> cart = getCart(session);
        
                Set<Long> ids =  cart.isEmpty() ? new HashSet<>() : cart.keySet();
                
            .collectList()
            .flatMap(cartItems -> {
                if (cartItems.isEmpty()) {
                    return Mono.just(List.<CartItemDto>of());
                }
                
                Set<Long> itemIds = cartItems.stream()
                    .map(CartItem::getItemId)
                    .collect(Collectors.toSet());
                
                return itemService.getItemByIds(itemIds)
                    .map(item -> {
                        CartItem cartItem = cartItems.stream()
                            .filter(ci -> ci.getItemId().equals(item.getId()))
                            .findFirst()
                            .orElse(null);
                        
                        int quantity = cartItem != null ? cartItem.getQuantity() : 0;
                        return new CartItemDto(item, quantity);
                    })
                    .collectList();
            });
    }

    public Mono<Double> getCartTotalReactive(Long userId) {
        return getCartItemsReactive(userId)
            .map(cartItems -> cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getCount())
                .sum());
    }
    
    public Mono<Boolean> isCheckoutEnabled(Long userId) {
        return Mono.zip(
                getCartTotalReactive(userId),
                paymentClientService.getBalance()
            )
            .map(tuple -> {
                Double cartTotal = tuple.getT1();
                Double balance = tuple.getT2();
                return balance >= cartTotal;
            })
            .onErrorReturn(false);
    }

    // Методы для работы с сессией (оставляем для обратной совместимости)
    public void addItemToCart(Long itemId, org.springframework.web.server.WebSession session) {
        // Этот метод оставляем для обратной совместимости, но он больше не используется
        throw new UnsupportedOperationException("Use addItemToCart(Long itemId, Long userId) instead");
    }

    public void removeItemFromCart(Long itemId, org.springframework.web.server.WebSession session) {
        throw new UnsupportedOperationException("Use removeItemFromCart(Long itemId, Long userId) instead");
    }

    public Mono<Void> addItemToCartReactive(Long itemId, org.springframework.web.server.WebSession session) {
        throw new UnsupportedOperationException("Use addItemToCart(Long itemId, Long userId) instead");
    }

    public Mono<Void> removeItemFromCartReactive(Long itemId, org.springframework.web.server.WebSession session) {
        throw new UnsupportedOperationException("Use removeItemFromCart(Long itemId, Long userId) instead");
    }

    public Mono<Void> deleteItemFromCartReactive(Long itemId, org.springframework.web.server.WebSession session) {
        throw new UnsupportedOperationException("Use deleteItemFromCart(Long itemId, Long userId) instead");
    }

    public Map<Long, Integer> getCart(org.springframework.web.server.WebSession session) {
        throw new UnsupportedOperationException("Use getCartItemsReactive(Long userId) instead");
    }

    public Mono<List<CartItemDto>> getCartItemsReactive(org.springframework.web.server.WebSession session) {
        throw new UnsupportedOperationException("Use getCartItemsReactive(Long userId) instead");
    }

    public Mono<Double> getCartTotalReactive(org.springframework.web.server.WebSession session) {
        throw new UnsupportedOperationException("Use getCartTotalReactive(Long userId) instead");
    }
    
    public Mono<Boolean> isCheckoutEnabled(org.springframework.web.server.WebSession session) {
        throw new UnsupportedOperationException("Use isCheckoutEnabled(Long userId) instead");
    }
}
