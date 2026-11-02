package com.foodcourt.menu.event.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientsReservedEvent {
    private String eventId;
    private String correlationId;
    private Long orderId;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
