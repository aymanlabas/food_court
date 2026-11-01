package com.foodcourt.menu.dto;

import com.foodcourt.menu.entity.Categorie;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishRequest {

    @NotBlank(message = "Le nom du plat est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal prix;

    @NotNull(message = "La catégorie est obligatoire")
    private Categorie categorie;

    @Size(max = 500, message = "Les allergènes ne doivent pas dépasser 500 caractères")
    private String allergenes;

    private Boolean disponible;

    @Size(max = 500, message = "L'URL de l'image ne doit pas dépasser 500 caractères")
    private String imageUrl;
}
