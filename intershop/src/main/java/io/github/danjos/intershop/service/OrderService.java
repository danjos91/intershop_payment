package io.github.danjos.intershop.service;

import io.github.danjos.intershop.exception.NotFoundException;
import io.github.danjos.intershop.model.*;
import io.github.danjos.intershop.repository.OrderRepository;
import io.github.danjos.intershop.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemService itemService;
    private final PaymentClientService paymentClientService;

    public Mono<Order> createOrderFromCart(Map<Long, Integer> cartItems, User user) {
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PROCESSING");
        Set<Long> itemIds = cartItems.keySet();

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    return itemService.getItemByIds(itemIds)
                            .map(item -> {
                                        OrderItem orderItem = new OrderItem();
                                        orderItem.setOrderId(savedOrder.getId());
                                        orderItem.setItemId(item.getId());
                                        orderItem.setQuantity(cartItems.get(item.getId()));
                                        orderItem.setPrice(item.getPrice());
                                        orderItem.setOrder(savedOrder);
                                        orderItem.setItem(item);
                                        return orderItem;
                                    })
                                    .flatMap(orderItemRepository::save)
                                    .collectList()
                                    .map(savedOrderItems -> {
                                        savedOrder.setItems(savedOrderItems);
                                        return savedOrder;
                                    });
                });
    }

    public Flux<Order> getUserOrders(User user) {
        return orderRepository.findByUserId(user.getId())
                .flatMap(this::populateOrderWithItems);
    }

    public Mono<Order> getOrderById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Order with id " + id + " not found")))
                .flatMap(this::populateOrderWithItems);
    }

    public Mono<Double> calculateOrderTotal(Map<Long, Integer> cartItems) {
        Set<Long> itemIds = cartItems.keySet();
        
        return itemService.getItemByIds(itemIds)
                .map(item -> {
                    Integer quantity = cartItems.get(item.getId());
                    return item.getPrice() * quantity;
                })
                .reduce(0.0, Double::sum);
    }

    public Mono<Boolean> processOrderPayment(Order order) {
        return calculateOrderTotalFromOrder(order)
                .flatMap(total -> {
                    String orderId = "order-" + order.getId();
                    return paymentClientService.processPayment(total, orderId)
                            .flatMap(success -> {
                                if (success) {
                                    order.setStatus("PAID");
                                    return orderRepository.save(order)
                                            .map(savedOrder -> true);
                                } else {
                                    order.setStatus("PAYMENT_FAILED");
                                    return orderRepository.save(order)
                                            .map(savedOrder -> false);
                                }
                            });
                });
    }

    public Mono<Double> getAvailableBalance() {
        return paymentClientService.getBalance();
    }

    public Mono<Boolean> canProcessPayment(Map<Long, Integer> cartItems) {
        return calculateOrderTotal(cartItems)
                .flatMap(total -> 
                    getAvailableBalance()
                            .map(balance -> balance >= total)
                );
    }

    private Mono<Double> calculateOrderTotalFromOrder(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return Mono.just(0.0);
        }
        
        return Mono.just(order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum());
    }

    private Mono<Order> populateOrderWithItems(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .flatMap(orderItems -> {
                    Set<Long> itemIds = orderItems.stream()
                            .map(OrderItem::getItemId)
                            .collect(Collectors.toSet());
                    
                    return itemService.getItemByIds(itemIds)
                            .collectMap(Item::getId)
                            .map(itemMap -> {
                                orderItems.forEach(orderItem -> {
                                    orderItem.setItem(itemMap.get(orderItem.getItemId()));
                                    orderItem.setOrder(order);
                                });
                                order.setItems(orderItems);
                                return order;
                            });
                });
    }
}
