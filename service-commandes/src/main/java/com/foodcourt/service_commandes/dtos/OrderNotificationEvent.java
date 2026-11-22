package com.foodcourt.service_commandes.dtos;

public record OrderNotificationEvent(
        Long userId,
        Long orderId,
        String recipient,
        String type,
        String message
) {}