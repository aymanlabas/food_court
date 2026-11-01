package com.foodcourt.usagers.controller;

import com.foodcourt.usagers.dto.DebitRequest;
import com.foodcourt.usagers.dto.DebitResponse;
import com.foodcourt.usagers.dto.PaymentResponse;
import com.foodcourt.usagers.dto.RefundRequest;
import com.foodcourt.usagers.dto.UserResponse;
import com.foodcourt.usagers.service.PaymentService;
import com.foodcourt.usagers.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/internal/usagers")
@RequiredArgsConstructor
@Tag(name = "Internal Usagers", description = "Endpoints internes pour la communication inter-microservices")
public class InternalUsagersController {

    private final UserService userService;
    private final PaymentService paymentService;

    @GetMapping("/users/{id}")
    @Operation(summary = "Obtenir les informations utilisateur pour validation d'une commande")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/users/{id}/balance")
    @Operation(summary = "Consulter le solde badge cantine d'un utilisateur")
    public ResponseEntity<BigDecimal> getUserBalance(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserBalance(id));
    }

    @PostMapping("/payments/debit")
    @Operation(summary = "Effectuer le débit du badge cantine de façon idempotente")
    public ResponseEntity<DebitResponse> debitBadge(@Valid @RequestBody DebitRequest request) {
        return ResponseEntity.ok(paymentService.debitBadge(request));
    }

    @PostMapping("/payments/refund")
    @Operation(summary = "Effectuer le remboursement d'une commande annulée de façon idempotente")
    public ResponseEntity<PaymentResponse> refundPayment(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refundPayment(request));
    }
}

