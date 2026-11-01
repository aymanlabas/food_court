package com.foodcourt.commandes.feign.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String nom;
    private String email;
    private BigDecimal soldeCantine;
    private String regimeAlimentaire;
    private String role;
}

