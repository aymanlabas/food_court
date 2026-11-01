package com.foodcourt.commandes.feign.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitResponse {
    private Long paymentId;
    private String statut;
}

