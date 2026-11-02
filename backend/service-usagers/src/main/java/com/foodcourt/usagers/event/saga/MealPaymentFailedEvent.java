package com.foodcourt.usagers.event.saga;

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
public class MealPaymentFailedEvent {
    private String eventId;
    private String correlationId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String reason;
    @Builder.Default
    private Instant timestamp = Instant.now();
}

