package com.foodcourt.serviceusagers.controllers;

import com.foodcourt.serviceusagers.dtos.PaiementResponse;
import com.foodcourt.serviceusagers.services.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * V4 OpenFeign - Contrôleur de paiement.
 *
 * Expose l'endpoint de débit du badge cantine.
 * Le service-usagers appelle service-commandes via Feign pour vérifier le ticket
 * avant de débiter le solde.
 *
 * Exemple : POST /api/usagers/clients/1/payer?orderId=5
 */
@RestController
@RequestMapping("/api/usagers/clients")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping("/{clientId}/payer")
    public ResponseEntity<PaiementResponse> payer(
            @PathVariable Long clientId,
            @RequestParam Long orderId) {
        PaiementResponse response = paiementService.payerCommande(clientId, orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{clientId}/recharger")
    public ResponseEntity<PaiementResponse> recharger(
            @PathVariable Long clientId,
            @RequestParam double montant) {
        return ResponseEntity.ok(paiementService.rechargerSolde(clientId, montant));
    }
}
