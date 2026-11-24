package com.yousef.servicemenu.dtos.request;



import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RecipeLineRequest {

    @NotNull(message = "L'identifiant du plat est obligatoire")
    @Positive(message = "L'identifiant du plat doit être positif")
    private Long dishId;

    @NotNull(message = "L'identifiant de l'ingrédient est obligatoire")
    @Positive(message = "L'identifiant de l'ingrédient doit être positif")
    private Long ingredientId;

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.001", message = "La quantité doit être supérieure à zéro")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal quantite;
}
