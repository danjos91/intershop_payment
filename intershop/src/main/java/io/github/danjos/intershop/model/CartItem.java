package io.github.danjos.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Table("cart_items")
@Data
public class CartItem {
    @Id
    private Long id;
    @Column("user_id")
    private Long userId;
    @Column("item_id")
    private Long itemId;
    private Integer quantity;
    @Column("created_at")
    private LocalDateTime createdAt;
}
