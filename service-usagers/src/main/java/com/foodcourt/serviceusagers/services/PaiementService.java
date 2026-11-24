package com.foodcourt.serviceusagers.services;

import com.foodcourt.serviceusagers.clients.CommandesFeignClient;
import com.foodcourt.serviceusagers.dtos.MealOrderDTO;
import com.foodcourt.serviceusagers.dtos.PaiementResponse;
import com.foodcourt.serviceusagers.entities.Client;
import com.foodcourt.serviceusagers.exceptions.RessourceIntrouvableException;
import com.foodcourt.serviceusagers.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * V4 OpenFeign - Service de paiement (débit du badge / solde cantine).
 *
 * Flux :
 * 1. Récupérer la commande depuis service-commandes via Feign
 * 2. Vérifier que le ticket existe et appartient bien au client
 * 3. Vérifier le solde cantine disponible
 * 4. Débiter le badge et sauvegarder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementService {

    private final ClientRepository clientRepository;
    private final CommandesFeignClient commandesFeignClient;

    @Transactional
    public PaiementResponse payerCommande(Long clientId, Long orderId) {
        // Étape 1 : Vérifier que le client existe
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RessourceIntrouvableException("Client introuvable : " + clientId));

        if (!client.isActif()) {
            throw new IllegalStateException("Le compte client ID " + clientId + " est désactivé.");
        }

        // ===== V4 OpenFeign : Vérification du ticket (commande) depuis service-commandes =====
        MealOrderDTO commande = commandesFeignClient.getCommandeById(orderId);
        if (commande == null) {
            throw new RessourceIntrouvableException("Commande introuvable avec ID : " + orderId);
        }
        log.info("Commande récupérée via Feign : id={}, userId={}, montant={}, statut={}",
                commande.id(), commande.userId(), commande.montantTotal(), commande.statut());

        // Étape 2 : Vérifier que la commande appartient au bon client
        if (!clientId.equals(commande.userId())) {
            throw new IllegalArgumentException(
                    "La commande #" + orderId + " n'appartient pas au client #" + clientId);
        }

        // Étape 3 : Vérifier le solde cantine
        double montant = commande.montantTotal() != null ? commande.montantTotal() : 0.0;
        if (client.getSolde() < montant) {
            throw new IllegalStateException(
                    "Solde cantine insuffisant. Solde : " + client.getSolde() + " MAD, montant dû : " + montant + " MAD");
        }

        // Étape 4 : Débiter le badge
        double ancienSolde = client.getSolde();
        client.setSolde(ancienSolde - montant);
        clientRepository.save(client);

        log.info("Badge débité pour client {} : {} MAD - {} MAD = {} MAD",
                clientId, ancienSolde, montant, client.getSolde());

        return new PaiementResponse(
                clientId,
                orderId,
                montant,
                client.getSolde(),
                "Paiement effectué avec succès. Nouveau solde : " + client.getSolde() + " MAD"
        );
    }

    @Transactional
    public PaiementResponse rechargerSolde(Long clientId, double montant) {
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant de recharge doit être positif.");
        }
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RessourceIntrouvableException("Client introuvable : " + clientId));
        client.setSolde(client.getSolde() + montant);
        clientRepository.save(client);

        log.info("Badge rechargé pour client {} : +{} MAD -> nouveau solde = {} MAD",
                clientId, montant, client.getSolde());

        return new PaiementResponse(
                clientId,
                null,
                montant,
                client.getSolde(),
                "Badge rechargé avec succès. Nouveau solde : " + client.getSolde() + " MAD"
        );
    }
}
