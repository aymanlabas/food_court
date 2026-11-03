package com.foodcourt.serviceusagers.dtos;

import java.time.Instant;

public record ClientResponse(
        Long id,
        String nom,
        String email,
        String telephone,
        String adresseLivraison,
        boolean actif,
        Instant creeLe
) {
}
