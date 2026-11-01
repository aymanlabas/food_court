package com.foodcourt.commandes.feign.dto;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishAvailabilityRequest {
    private Map<Long, Integer> dishQuantities;
}

