package com.foodcourt.service_commandes.dtos.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishDTO {
    private Long id;
    private String nom;
    private String description;
    private Double prix;
    private Boolean disponible;
}