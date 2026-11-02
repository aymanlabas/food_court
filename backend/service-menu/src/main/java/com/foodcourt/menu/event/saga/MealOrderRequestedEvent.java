package com.foodcourt.menu.event.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealOrderRequestedEvent {
    private String eventId;
    private String correlationId;
    private Long orderId;
    private Long userId;
    private List<OrderItemPayload> items;
    private BigDecimal totalAmount;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
