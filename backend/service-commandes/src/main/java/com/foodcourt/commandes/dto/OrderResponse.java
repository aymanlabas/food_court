package com.foodcourt.commandes.dto;

import com.foodcourt.commandes.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime date;
    private BigDecimal montantTotal;
    private OrderStatus statut;
    private UUID idempotencyKey;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

