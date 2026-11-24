package com.yousef.servicemenu.dtos.request;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class DishRequest {

    @NotBlank(message = "Le nom du plat est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à zéro")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal prix;

    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50)
    private String categorie;

    @NotNull(message = "La liste des allergènes est obligatoire")
    private Set<@NotBlank String> allergenes;

    @NotNull(message = "La disponibilité est obligatoire")
    private Boolean disponible;
}

