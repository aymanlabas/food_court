package com.foodcourt.commandes.event.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private String eventId;
    private String correlationId;
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    @Builder.Default
    private Instant timestamp = Instant.now();
}

