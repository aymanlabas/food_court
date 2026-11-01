package com.foodcourt.commandes.event;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent implements Serializable {

    private UUID eventId;
    private Long orderId;
    private Long userId;
    private LocalDateTime timestamp;
    private BigDecimal refundedAmount;
    private String reason;
}

