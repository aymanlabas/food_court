package com.foodcourt.notification.dtos;

import com.foodcourt.notification.entities.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull Long userId,
        Long orderId,
        @NotBlank String recipient,
        @NotNull NotificationType type,
        @NotBlank String message
) {}
