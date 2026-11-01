package com.foodcourt.menu.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishAvailabilityRequest {

    @NotEmpty(message = "La map des plats ne peut pas être vide")
    private Map<Long, Integer> dishQuantities;
}
