package com.foodcourt.usagers.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String nom;

    @Email(message = "L'email doit être valide")
    private String email;

    private String regimeAlimentaire;
}
