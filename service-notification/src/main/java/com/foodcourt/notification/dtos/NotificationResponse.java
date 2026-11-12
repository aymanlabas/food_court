package com.foodcourt.notification.dtos;

import com.foodcourt.notification.entities.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id, Long userId, Long orderId, String recipient,
        NotificationType type, String message, boolean read, LocalDateTime createdAt
) {}
