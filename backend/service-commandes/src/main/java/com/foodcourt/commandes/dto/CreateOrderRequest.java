package com.foodcourt.commandes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "L'identifiant utilisateur est obligatoire")
    private Long userId;

    @NotEmpty(message = "Une commande doit contenir au moins un plat")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "La clé d'idempotence est obligatoire")
    private UUID idempotencyKey;
}

