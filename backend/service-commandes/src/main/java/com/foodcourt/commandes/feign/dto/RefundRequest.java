package com.foodcourt.commandes.feign.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private Long mealOrderId;
    private UUID idempotencyKey;
}

