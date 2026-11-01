package com.foodcourt.commandes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull(message = "L'identifiant du plat est obligatoire")
    private Long dishId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité minimum par plat est de 1")
    private Integer quantite;
}

