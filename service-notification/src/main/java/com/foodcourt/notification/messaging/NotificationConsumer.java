package com.foodcourt.notification.messaging;

import com.foodcourt.notification.config.RabbitMQConfig;
import com.foodcourt.notification.dtos.NotificationRequest;
import com.foodcourt.notification.entities.NotificationType;
import com.foodcourt.notification.services.EmailService;
import com.foodcourt.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * V5 RabbitMQ - Consommateur de messages du service-commandes.
 *
 * Reçoit un Map (JSON brut) depuis la queue pour éviter les problèmes de
 * désérialisation inter-services (NotificationType enum vs String).
 * Reconstruit manuellement le NotificationRequest avec l'enum correct.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeOrderEvent(Map<String, Object> payload) {
        log.info("Message reçu depuis RabbitMQ : {}", payload);

        try {
            Long userId = payload.get("userId") != null
                    ? Long.valueOf(payload.get("userId").toString()) : null;
            Long orderId = payload.get("orderId") != null
                    ? Long.valueOf(payload.get("orderId").toString()) : null;
            String recipient = (String) payload.get("recipient");
            String message = (String) payload.get("message");

            // Conversion String → enum NotificationType (robuste, sans crash si valeur inconnue)
            NotificationType type;
            try {
                type = NotificationType.valueOf((String) payload.get("type"));
            } catch (Exception e) {
                log.warn("Type de notification inconnu : {}. Utilisation de INFORMATION.", payload.get("type"));
                type = NotificationType.INFORMATION;
            }

            NotificationRequest request = new NotificationRequest(userId, orderId, recipient, type, message);
            notificationService.create(request);
            emailService.sendNotificationEmail(request);

        } catch (Exception e) {
            log.error("Erreur lors du traitement du message RabbitMQ : {}", e.getMessage(), e);
        }
    }
}
