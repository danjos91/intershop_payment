package io.github.danjos.intershop.controller;

import io.github.danjos.intershop.service.OrderService;
import io.github.danjos.intershop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequiredArgsConstructor
public class WebOrderController {
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> showOrders() {
        return userService.getCurrentUser()
            .flatMap(user -> 
                orderService.getUserOrders(user)
                    .collectList()
                    .map(orders -> 
                        Rendering.view("orders")
                            .modelAttribute("orders", orders)
                            .build()
                    )
            );
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<Rendering> showOrder(
            @PathVariable Long id, 
            @RequestParam(required = false) boolean newOrder) {
        return orderService.getOrderById(id)
            .map(order -> 
                Rendering.view("order")
                    .modelAttribute("order", order)
                    .modelAttribute("newOrder", newOrder)
                    .build()
            );
    }
} 