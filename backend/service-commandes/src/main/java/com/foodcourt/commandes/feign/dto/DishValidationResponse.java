package com.foodcourt.commandes.feign.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishValidationResponse {
    private Long dishId;
    private String nom;
    private BigDecimal prix;
    private boolean disponible;
    private boolean valid;
    private String message;
}

