package com.foodcourt.usagers.dto;

import com.foodcourt.usagers.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitResponse {

    private Long paymentId;
    private PaymentStatus statut;
}
