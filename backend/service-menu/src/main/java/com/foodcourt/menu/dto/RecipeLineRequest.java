package com.foodcourt.menu.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeLineRequest {

    @NotNull(message = "L'identifiant du plat est obligatoire")
    private Long dishId;

    @NotNull(message = "L'identifiant de l'ingrédient est obligatoire")
    private Long ingredientId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantite;
}
