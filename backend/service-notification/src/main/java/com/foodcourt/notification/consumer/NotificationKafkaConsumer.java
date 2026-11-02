package com.foodcourt.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcourt.notification.config.KafkaTopics;
import com.foodcourt.notification.entity.Notification;
import com.foodcourt.notification.entity.NotificationType;
import com.foodcourt.notification.event.saga.OrderCancelledKafkaEvent;
import com.foodcourt.notification.event.saga.OrderReadyKafkaEvent;
import com.foodcourt.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consumes order-ready event from Kafka and notifies the employee.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_READY, groupId = "service-notification-group")
    @Transactional
    public void onOrderReady(String message) {
        try {
            OrderReadyKafkaEvent event = objectMapper.readValue(message, OrderReadyKafkaEvent.class);
            log.info("Received Kafka OrderReadyEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());

            UUID eventUuid = parseUuidSafely(event.getEventId());
            if (notificationRepository.existsByEventId(eventUuid)) {
                log.info("Kafka event with eventId {} already processed, skipping duplicate", event.getEventId());
                return;
            }

            String dishes = (event.getDishNames() != null && !event.getDishNames().isEmpty())
                    ? String.join(", ", event.getDishNames())
                    : "votre plateau repas";

            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .mealOrderId(event.getOrderId())
                    .type(NotificationType.COMMANDE_PRETE)
                    .title("Votre commande est prête ! 🍽️")
                    .message(String.format("Votre commande #%d (%s) est prête à être récupérée au comptoir de la cantine.",
                            event.getOrderId(), dishes))
                    .eventId(eventUuid)
                    .read(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Saved ready notification from Kafka for user {} and order {}", event.getUserId(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process Kafka OrderReadyEvent: {}", message, e);
            throw new RuntimeException("Error processing Kafka OrderReadyEvent", e);
        }
    }

    /**
     * Consumes order-cancelled event from Kafka and notifies the employee.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "service-notification-group")
    @Transactional
    public void onOrderCancelled(String message) {
        try {
            OrderCancelledKafkaEvent event = objectMapper.readValue(message, OrderCancelledKafkaEvent.class);
            log.info("Received Kafka OrderCancelledEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());

            UUID eventUuid = parseUuidSafely(event.getEventId());
            if (notificationRepository.existsByEventId(eventUuid)) {
                log.info("Kafka event with eventId {} already processed, skipping duplicate", event.getEventId());
                return;
            }

            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .mealOrderId(event.getOrderId())
                    .type(NotificationType.COMMANDE_ANNULEE)
                    .title("Commande annulée & remboursée ↩️")
                    .message(String.format("La commande #%d a été annulée. Un montant de %.2f € a été recrédité sur votre badge cantine. Motif : %s",
                            event.getOrderId(),
                            event.getRefundedAmount() != null ? event.getRefundedAmount() : 0.0,
                            event.getReason() != null ? event.getReason() : "Non spécifié"))
                    .eventId(eventUuid)
                    .read(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Saved cancellation notification from Kafka for user {} and order {}", event.getUserId(), event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process Kafka OrderCancelledEvent: {}", message, e);
            throw new RuntimeException("Error processing Kafka OrderCancelledEvent", e);
        }
    }

    private UUID parseUuidSafely(String id) {
        if (id == null) return UUID.randomUUID();
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(id.getBytes());
        }
    }
}

