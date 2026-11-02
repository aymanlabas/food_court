package com.foodcourt.commandes.event.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadyEvent {
    private String eventId;
    private String correlationId;
    private Long orderId;
    private Long userId;
    @Builder.Default
    private Instant timestamp = Instant.now();
}

