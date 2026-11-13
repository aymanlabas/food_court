package com.yousef.servicemenu.dtos.response;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class DishResponse {

    private Long id;
    private String nom;
    private BigDecimal prix;
    private String categorie;
    private Set<String> allergenes;
    private boolean disponible;
}

