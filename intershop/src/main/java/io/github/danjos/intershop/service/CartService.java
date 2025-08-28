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

    // Методы для работы с корзиной пользователя в БД
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

    // Методы для очистки корзины пользователя (например, после создания заказа)
    public Mono<Void> clearUserCart(Long userId) {
        return cartItemRepository.deleteByUserId(userId);
    }
}
