package com.foodcourt.commandes.feign.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitRequest {
    private Long userId;
    private Long mealOrderId;
    private BigDecimal montant;
    private UUID idempotencyKey;
}

