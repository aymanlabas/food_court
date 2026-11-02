package com.foodcourt.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcourt.notification.entity.Notification;
import com.foodcourt.notification.entity.NotificationType;
import com.foodcourt.notification.event.saga.OrderCancelledKafkaEvent;
import com.foodcourt.notification.event.saga.OrderReadyKafkaEvent;
import com.foodcourt.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationKafkaConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificationKafkaConsumer notificationKafkaConsumer;

    @Test
    void shouldStoreNotificationOnOrderReady() throws Exception {
        UUID eventId = UUID.randomUUID();
        OrderReadyKafkaEvent event = OrderReadyKafkaEvent.builder()
                .eventId(eventId.toString())
                .correlationId("corr-notif-1")
                .orderId(500L)
                .userId(2L)
                .dishNames(List.of("Burger Maison", "Frites"))
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(notificationRepository.existsByEventId(eventId)).thenReturn(false);

        notificationKafkaConsumer.onOrderReady(json);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        Notification saved = captor.getValue();

        assertEquals(2L, saved.getUserId());
        assertEquals(500L, saved.getMealOrderId());
        assertEquals(NotificationType.COMMANDE_PRETE, saved.getType());
        assertFalse(saved.isRead());
    }

    @Test
    void shouldStoreNotificationOnOrderCancelled() throws Exception {
        UUID eventId = UUID.randomUUID();
        OrderCancelledKafkaEvent event = OrderCancelledKafkaEvent.builder()
                .eventId(eventId.toString())
                .correlationId("corr-notif-2")
                .orderId(501L)
                .userId(3L)
                .reason("Rupture de stock")
                .refundedAmount(new BigDecimal("15.00"))
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(notificationRepository.existsByEventId(eventId)).thenReturn(false);

        notificationKafkaConsumer.onOrderCancelled(json);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        Notification saved = captor.getValue();

        assertEquals(3L, saved.getUserId());
        assertEquals(501L, saved.getMealOrderId());
        assertEquals(NotificationType.COMMANDE_ANNULEE, saved.getType());
    }

    @Test
    void shouldSkipDuplicateNotification() throws Exception {
        UUID eventId = UUID.randomUUID();
        OrderReadyKafkaEvent event = OrderReadyKafkaEvent.builder()
                .eventId(eventId.toString())
                .orderId(500L)
                .userId(2L)
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(notificationRepository.existsByEventId(eventId)).thenReturn(true);

        notificationKafkaConsumer.onOrderReady(json);

        verify(notificationRepository, never()).save(any());
    }
}
