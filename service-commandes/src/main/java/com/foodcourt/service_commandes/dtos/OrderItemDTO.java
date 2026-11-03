package com.foodcourt.service_commandes.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemDTO(
    @NotNull(message = "dishId est obligatoire") Long dishId,
    @NotNull(message = "quantite est obligatoire") @Positive(message = "quantite doit être positive") Integer quantite,
    @NotNull(message = "prixUnitaire est obligatoire") @Positive(message = "prixUnitaire doit être positif") Double prixUnitaire
) {}