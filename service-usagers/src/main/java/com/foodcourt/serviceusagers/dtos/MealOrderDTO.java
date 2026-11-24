package com.foodcourt.serviceusagers.dtos;

/**
 * DTO représentant une commande reçue depuis service-commandes via Feign.
 */
public record MealOrderDTO(
        Long id,
        Long userId,
        Double montantTotal,
        String statut
) {}
