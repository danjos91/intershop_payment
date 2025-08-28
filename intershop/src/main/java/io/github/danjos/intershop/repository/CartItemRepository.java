package io.github.danjos.intershop.repository;

import io.github.danjos.intershop.model.CartItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    
    @Query("SELECT * FROM cart_items WHERE user_id = :userId")
    Flux<CartItem> findByUserId(Long userId);
    
    @Query("SELECT * FROM cart_items WHERE user_id = :userId AND item_id = :itemId")
    Mono<CartItem> findByUserIdAndItemId(Long userId, Long itemId);
    
    @Query("DELETE FROM cart_items WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);
    
    @Query("DELETE FROM cart_items WHERE user_id = :userId AND item_id = :itemId")
    Mono<Void> deleteByUserIdAndItemId(Long userId, Long itemId);
}
