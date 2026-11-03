package com.foodcourt.serviceusagers.dtos;

import java.time.Instant;

public record LivreurResponse(
        Long id,
        String nom,
        String email,
        String telephone,
        String vehicule,
        boolean disponible,
        boolean actif,
        Instant creeLe
) {
}
