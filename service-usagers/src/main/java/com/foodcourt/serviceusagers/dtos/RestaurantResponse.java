package com.foodcourt.serviceusagers.dtos;

import java.time.Instant;

public record RestaurantResponse(
        Long id,
        String nom,
        String email,
        String telephone,
        String adresse,
        String horaires,
        boolean actif,
        Instant creeLe
) {
}
