package com.foodcourt.service_commandes.dtos;

/**
 * Événement publié par le service-commandes vers RabbitMQ.
 *
 * IMPORTANT : Le champ "type" doit correspondre aux valeurs de l'enum NotificationType
 * du service-notification : COMMANDE_RECUE | COMMANDE_PRETE | COMMANDE_ANNULEE | INFORMATION
 *
 * Jackson sérialise ce record en JSON avec le champ "type" comme String.
 * Le service-notification désérialise "type" vers NotificationType enum par son nom.
 */
public record OrderNotificationEvent(
        Long userId,
        Long orderId,
        String recipient,
        String type,
        String message
) {}
