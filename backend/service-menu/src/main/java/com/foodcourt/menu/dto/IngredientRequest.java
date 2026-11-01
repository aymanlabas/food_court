package com.foodcourt.menu.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequest {

    @NotBlank(message = "Le nom de l'ingrédient est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotNull(message = "Le stock est obligatoire")
    @DecimalMin(value = "0.0", message = "Le stock ne peut pas être négatif")
    private BigDecimal stock;

    @NotBlank(message = "L'unité est obligatoire")
    @Size(max = 20, message = "L'unité ne doit pas dépasser 20 caractères")
    private String unite;

    @NotNull(message = "Le seuil d'alerte est obligatoire")
    @DecimalMin(value = "0.0", message = "Le seuil d'alerte ne peut pas être négatif")
    private BigDecimal seuilAlerte;
}
