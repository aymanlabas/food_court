package com.foodcourt.service_commandes.dtos.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeLineDTO {
    private Long id;
    private Long dishId;
    private String dishNom;
    private Long ingredientId;
    private String ingredientNom;
    private BigDecimal quantite;
}
