package com.foodcourt.menu.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.0", message = "La quantité ne peut pas être négative")
    private BigDecimal quantite;
}
