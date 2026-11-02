package com.foodcourt.notification.event.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadyKafkaEvent {
    private String eventId;
    private String correlationId;
    private Long orderId;
    private Long userId;
    private List<String> dishNames;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
