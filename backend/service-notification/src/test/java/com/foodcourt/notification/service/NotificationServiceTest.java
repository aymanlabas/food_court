package com.foodcourt.notification.service;

import com.foodcourt.notification.entity.Notification;
import com.foodcourt.notification.entity.NotificationType;
import com.foodcourt.notification.event.OrderCancelledEvent;
import com.foodcourt.notification.event.OrderReadyEvent;
import com.foodcourt.notification.mapper.NotificationMapper;
import com.foodcourt.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private UUID eventId;
    private OrderReadyEvent readyEvent;
    private OrderCancelledEvent cancelledEvent;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();

        readyEvent = OrderReadyEvent.builder()
                .eventId(eventId)
                .orderId(10L)
                .userId(1L)
                .timestamp(LocalDateTime.now())
                .montantTotal(new BigDecimal("15.50"))
                .dishNames(List.of("Salade César", "Fondant Chocolat"))
                .build();

        cancelledEvent = OrderCancelledEvent.builder()
                .eventId(eventId)
                .orderId(10L)
                .userId(1L)
                .timestamp(LocalDateTime.now())
                .refundedAmount(new BigDecimal("15.50"))
                .reason("Annulation client")
                .build();
    }

    @Test
    void shouldProcessOrderReadyEventSuccessfully() {
        when(notificationRepository.existsByEventId(eventId)).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(100L);
            return n;
        });

        notificationService.processOrderReady(readyEvent);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void shouldIgnoreDuplicateOrderReadyEventIdempotently() {
        when(notificationRepository.existsByEventId(eventId)).thenReturn(true);

        notificationService.processOrderReady(readyEvent);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void shouldProcessOrderCancelledEventSuccessfully() {
        when(notificationRepository.existsByEventId(eventId)).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(101L);
            return n;
        });

        notificationService.processOrderCancelled(cancelledEvent);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void shouldIgnoreDuplicateOrderCancelledEventIdempotently() {
        when(notificationRepository.existsByEventId(eventId)).thenReturn(true);

        notificationService.processOrderCancelled(cancelledEvent);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void shouldMarkNotificationAsRead() {
        Notification notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .read(false)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenReturn(notification);

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }
}

