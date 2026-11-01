package com.foodcourt.commandes.dto;

import com.foodcourt.commandes.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private OrderStatus statut;
}

