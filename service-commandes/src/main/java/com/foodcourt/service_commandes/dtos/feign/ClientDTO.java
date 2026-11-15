package com.foodcourt.service_commandes.dtos.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {
    private Long id;
    private String nom;
    private String email;
    private String telephone;
    private String adresseLivraison;
    private Boolean actif;
}