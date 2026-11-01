package com.foodcourt.notification.service;

import com.foodcourt.notification.dto.NotificationResponse;
import com.foodcourt.notification.entity.Notification;
import com.foodcourt.notification.entity.NotificationType;
import com.foodcourt.notification.event.OrderCancelledEvent;
import com.foodcourt.notification.event.OrderReadyEvent;
import com.foodcourt.notification.mapper.NotificationMapper;
import com.foodcourt.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Traitement idempotent de l'événement Commande Prête.
     */
    @Transactional
    public void processOrderReady(OrderReadyEvent event) {
        log.info("Processing OrderReadyEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());

        // Idempotence : Ne pas enregistrer deux fois la même notification
        if (notificationRepository.existsByEventId(event.getEventId())) {
            log.warn("Event with eventId {} already processed, skipping duplicate", event.getEventId());
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
                .eventId(event.getEventId())
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification stored with ID: {}", saved.getId());
    }

    /**
     * Traitement idempotent de l'événement Commande Annulée.
     */
    @Transactional
    public void processOrderCancelled(OrderCancelledEvent event) {
        log.info("Processing OrderCancelledEvent: orderId={}, eventId={}", event.getOrderId(), event.getEventId());

        if (notificationRepository.existsByEventId(event.getEventId())) {
            log.warn("Event with eventId {} already processed, skipping duplicate", event.getEventId());
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
                .eventId(event.getEventId())
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification stored with ID: {}", saved.getId());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notificationMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadUserNotifications(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        return notificationMapper.toResponseList(list);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification non trouvée : " + id));
        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}
