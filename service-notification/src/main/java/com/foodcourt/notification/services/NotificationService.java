package com.foodcourt.notification.services;

import com.foodcourt.notification.dtos.NotificationRequest;
import com.foodcourt.notification.dtos.NotificationResponse;
import com.foodcourt.notification.entities.Notification;
import com.foodcourt.notification.exceptions.NotificationNotFoundException;
import com.foodcourt.notification.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;

    @Transactional
    public NotificationResponse create(NotificationRequest request) {
        Notification notification = repository.save(Notification.builder()
                .userId(request.userId())
                .orderId(request.orderId())
                .recipient(request.recipient())
                .type(request.type())
                .message(request.message())
                .build());
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.setRead(true);
        return toResponse(repository.save(notification));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(n.getId(), n.getUserId(), n.getOrderId(),
                n.getRecipient(), n.getType(), n.getMessage(), n.isRead(), n.getCreatedAt());
    }
}
