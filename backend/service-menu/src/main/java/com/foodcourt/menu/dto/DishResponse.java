package com.foodcourt.menu.dto;

import com.foodcourt.menu.entity.Categorie;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishResponse {

    private Long id;
    private String nom;
    private String description;
    private BigDecimal prix;
    private Categorie categorie;
    private String allergenes;
    private boolean disponible;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
