package com.foodcourt.menu.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeLineResponse {

    private Long id;
    private Long dishId;
    private String dishNom;
    private Long ingredientId;
    private String ingredientNom;
    private BigDecimal quantite;
}
