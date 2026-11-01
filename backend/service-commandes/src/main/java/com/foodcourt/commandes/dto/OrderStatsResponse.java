package com.foodcourt.commandes.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsResponse {

    private long totalOrders;
    private long recuOrders;
    private long enPreparationOrders;
    private long pretOrders;
    private long serviOrders;
    private long annuleOrders;
    private BigDecimal totalRevenue;
}

