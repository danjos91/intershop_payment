package io.github.danjos.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;

@Table("order_items")
@Data
public class OrderItem {
    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;
    @Column("item_id")
    private Long itemId;

    private int quantity;
    private double price;

    @Transient
    private Order order;
    @Transient
    private Item item;
}
