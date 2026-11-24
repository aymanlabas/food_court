package com.foodcourt.serviceusagers.dtos;

/**
 * Réponse retournée après un paiement (débit du badge cantine).
 */
public record PaiementResponse(
        Long clientId,
        Long orderId,
        Double montantDebite,
        Double nouveauSolde,
        String message
) {}
