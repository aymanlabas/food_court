package com.foodcourt.serviceusagers.clients;

import com.foodcourt.serviceusagers.dtos.MealOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback pour CommandesFeignClient si service-commandes est indisponible.
 */
@Component
@Slf4j
public class CommandesFeignClientFallback implements CommandesFeignClient {

    @Override
    public MealOrderDTO getCommandeById(Long id) {
        log.warn("Fallback: service-commandes indisponible pour commande ID={}", id);
        return null;
    }
}
