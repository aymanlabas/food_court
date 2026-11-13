package com.yousef.servicemenu.dtos.response;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class IngredientResponse {

    private Long id;
    private String nom;
    private BigDecimal stock;
    private String unite;
    private BigDecimal seuilAlerte;
}

