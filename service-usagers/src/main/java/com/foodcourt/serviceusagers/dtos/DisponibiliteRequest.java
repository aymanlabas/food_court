package com.foodcourt.serviceusagers.dtos;

import jakarta.validation.constraints.NotNull;

public record DisponibiliteRequest(
        @NotNull(message = "La disponibilité est obligatoire")
        Boolean disponible
) {
}
