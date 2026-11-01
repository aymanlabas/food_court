package com.foodcourt.commandes.feign.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientReservationRequest {
    private Map<Long, BigDecimal> ingredientQuantities;
}

