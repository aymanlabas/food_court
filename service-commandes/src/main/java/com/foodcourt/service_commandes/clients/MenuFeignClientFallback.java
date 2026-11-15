package com.foodcourt.service_commandes.clients;

import com.foodcourt.service_commandes.dtos.feign.DishDTO;
import org.springframework.stereotype.Component;

@Component
public class MenuFeignClientFallback implements MenuFeignClient {

    @Override
    public DishDTO getDishById(Long id) {
        return DishDTO.builder()
                .id(id)
                .nom("Plat #" + id + " (Service Menu Indisponible)")
                .description("Détails du plat non disponibles")
                .prix(0.0)
                .disponible(true)
                .build();
    }
}