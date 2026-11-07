package com.foodcourt.serviceusagers.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LivreurRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email est invalide")
        String email,

        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone,

        @NotBlank(message = "Le véhicule est obligatoire")
        String vehicule
) {
}
