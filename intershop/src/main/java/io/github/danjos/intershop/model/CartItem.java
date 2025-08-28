package io.github.danjos.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Table("cart_items")
@Data
public class CartItem {
    @Id
    private Long id;
    private Long userId;
    private Long itemId;
    private Integer quantity;
    private LocalDateTime createdAt;
}
