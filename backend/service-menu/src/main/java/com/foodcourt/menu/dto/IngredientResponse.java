package com.foodcourt.menu.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientResponse {

    private Long id;
    private String nom;
    private BigDecimal stock;
    private String unite;
    private BigDecimal seuilAlerte;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
