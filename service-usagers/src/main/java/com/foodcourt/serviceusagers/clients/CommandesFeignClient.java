package com.foodcourt.serviceusagers.clients;

import com.foodcourt.serviceusagers.dtos.MealOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * V4 OpenFeign - Client Feign du service-usagers vers service-commandes.
 *
 * Utilisé pour vérifier qu'un ticket (commande) existe et appartient bien
 * au client avant de débiter son badge (solde cantine).
 */
@FeignClient(name = "service-commandes", fallback = CommandesFeignClientFallback.class)
public interface CommandesFeignClient {

    @GetMapping("/api/orders/{id}")
    MealOrderDTO getCommandeById(@PathVariable("id") Long id);
}
