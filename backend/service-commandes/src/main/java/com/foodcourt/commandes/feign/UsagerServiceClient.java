package com.foodcourt.commandes.feign;

import com.foodcourt.commandes.feign.dto.DebitRequest;
import com.foodcourt.commandes.feign.dto.DebitResponse;
import com.foodcourt.commandes.feign.dto.RefundRequest;
import com.foodcourt.commandes.feign.dto.UserSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "service-usagers", url = "${services.service-usagers.url:http://localhost:8083}")
public interface UsagerServiceClient {

    @GetMapping("/api/internal/usagers/users/{id}")
    UserSummaryResponse getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/internal/usagers/users/{id}/balance")
    BigDecimal getUserBalance(@PathVariable("id") Long id);

    @PostMapping("/api/internal/usagers/payments/debit")
    DebitResponse debitBadge(@RequestBody DebitRequest request);

    @PostMapping("/api/internal/usagers/payments/refund")
    void refundPayment(@RequestBody RefundRequest request);
}

