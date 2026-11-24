package com.foodcourt.service_commandes.clients;

import com.foodcourt.service_commandes.dtos.feign.DishDTO;
import com.foodcourt.service_commandes.dtos.feign.RecipeLineDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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

    @Override
    public List<RecipeLineDTO> getAllRecipeLines() {
        return Collections.emptyList();
    }
}
