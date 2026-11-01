package com.foodcourt.menu.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientReservationRequest {

    @NotEmpty(message = "La liste des ingrédients ne peut pas être vide")
    private Map<Long, BigDecimal> ingredientQuantities;
}
