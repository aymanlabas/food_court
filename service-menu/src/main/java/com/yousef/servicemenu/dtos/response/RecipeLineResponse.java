package com.yousef.servicemenu.dtos.response;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RecipeLineResponse {

    private Long id;
    private Long dishId;
    private String dishNom;
    private Long ingredientId;
    private String ingredientNom;
    private BigDecimal quantite;
}

