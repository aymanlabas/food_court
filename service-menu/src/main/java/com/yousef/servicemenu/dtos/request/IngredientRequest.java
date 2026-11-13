package com.yousef.servicemenu.dtos.request;



import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class IngredientRequest {

    @NotBlank(message = "Le nom de l'ingrédient est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotNull(message = "Le stock est obligatoire")
    @DecimalMin(value = "0.0", message = "Le stock ne peut pas être négatif")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal stock;

    @NotBlank(message = "L'unité est obligatoire")
    @Size(max = 20)
    private String unite;

    @NotNull(message = "Le seuil d'alerte est obligatoire")
    @DecimalMin(value = "0.0", message = "Le seuil ne peut pas être négatif")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal seuilAlerte;
}

