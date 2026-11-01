package com.foodcourt.commandes.feign.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientReservationResponse {
    private boolean success;
    private String message;
    private List<String> insufficientIngredients;
    private Map<Long, String> details;
}

