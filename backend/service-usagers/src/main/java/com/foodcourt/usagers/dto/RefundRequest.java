package com.foodcourt.usagers.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotNull(message = "L'identifiant de commande est obligatoire")
    private Long mealOrderId;

    @NotNull(message = "La clé d'idempotence est obligatoire")
    private UUID idempotencyKey;
}
