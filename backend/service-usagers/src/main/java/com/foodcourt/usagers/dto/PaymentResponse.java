package com.foodcourt.usagers.dto;

import com.foodcourt.usagers.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long mealOrderId;
    private Long userId;
    private BigDecimal montant;
    private String moyen;
    private PaymentStatus statut;
    private UUID idempotencyKey;
    private LocalDateTime createdAt;
}
