package com.foodcourt.service_commandes.dtos.feign;

import com.foodcourt.service_commandes.entities.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderEnrichedResponseDTO(
        Long id,
        LocalDateTime date,
        OrderStatus statut,
        Double montantTotal,
        ClientDTO client,
        List<EnrichedItemDTO> items
) {
    public record EnrichedItemDTO(
            Long dishId,
            String nomPlat,
            Integer quantite,
            Double prixUnitaire,
            Double sousTotal
    ) {}
}