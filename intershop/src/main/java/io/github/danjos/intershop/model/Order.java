package io.github.danjos.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table("orders")
@Data
public class Order {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("order_date")
    private LocalDateTime orderDate;
    private String status;

    @Transient
    private List<OrderItem> items = new ArrayList<>();

    public double getTotalSum() {
        return items.stream().mapToDouble(oi -> oi.getPrice() * oi.getQuantity()).sum();
    }
}
