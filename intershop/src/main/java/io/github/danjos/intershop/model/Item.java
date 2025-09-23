package io.github.danjos.intershop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;

@Table("items")
@Data
public class Item {
    @Id
    private Long id;
    private String title;
    private String description;
    private double price;
    @Column("img_path")
    private String imgPath;
    private int stock;
}
