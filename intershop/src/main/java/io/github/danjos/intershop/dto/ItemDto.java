package io.github.danjos.intershop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemDto {
    
    @NotBlank(message = "Название товара не может быть пустым")
    @Size(min = 1, max = 100, message = "Название товара должно содержать от 1 до 100 символов")
    private String name;
    
    @NotBlank(message = "Описание товара не может быть пустым")
    @Size(min = 1, max = 500, message = "Описание товара должно содержать от 1 до 500 символов")
    private String description;
    
    @NotNull(message = "Цена товара не может быть пустой")
    @DecimalMin(value = "0.01", message = "Цена товара должна быть больше 0")
    private BigDecimal price;
    
    @NotNull(message = "Количество товара не может быть пустым")
    @DecimalMin(value = "0", message = "Количество товара не может быть отрицательным")
    private Integer stock;
}
