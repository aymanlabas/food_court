package com.foodcourt.service_commandes.dtos;

import com.foodcourt.service_commandes.entities.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record MealOrderResponseDTO(
    Long id,
    Long userId,
    LocalDateTime date,
    Double montantTotal,
    OrderStatus statut,
    List<OrderItemDTO> items
) {}