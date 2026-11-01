package com.foodcourt.notification.dto;

import com.foodcourt.notification.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Long userId;
    private Long mealOrderId;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private UUID eventId;
    private LocalDateTime createdAt;
}
