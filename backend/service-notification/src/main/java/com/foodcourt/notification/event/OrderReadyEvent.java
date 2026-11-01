package com.foodcourt.notification.event;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadyEvent implements Serializable {

    private UUID eventId;
    private Long orderId;
    private Long userId;
    private LocalDateTime timestamp;
    private BigDecimal montantTotal;
    private List<String> dishNames;
}
