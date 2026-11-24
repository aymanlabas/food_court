package com.foodcourt.service_commandes.dtos.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishDTO {
    private Long id;
    private String nom;
    private String description;
    private Double prix;           // legacy double field
    private BigDecimal prixExact;  // mapped from service-menu's BigDecimal prix
    private Boolean disponible;
    private String categorie;
}