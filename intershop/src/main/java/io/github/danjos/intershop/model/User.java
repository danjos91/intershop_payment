package io.github.danjos.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("users")
@Data
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String email;
    private BigDecimal balance;
    @Column("created_at")
    private LocalDateTime createdAt;
}