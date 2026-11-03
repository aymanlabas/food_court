package com.foodcourt.service_commandes.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record MealOrderRequestDTO(
    @NotNull(message = "userId est obligatoire") Long userId,
    @NotEmpty(message = "La commande doit contenir au moins un plat") @Valid List<OrderItemDTO> items
) {}