package com.foodcourt.commandes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcourt.commandes.config.KafkaTopics;
import com.foodcourt.commandes.entity.MealOrder;
import com.foodcourt.commandes.entity.OrderStatus;
import com.foodcourt.commandes.event.saga.*;
import com.foodcourt.commandes.outbox.OutboxPublisher;
import com.foodcourt.commandes.outbox.ProcessedEventRepository;
import com.foodcourt.commandes.repository.MealOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSagaTest {

    @Mock
    private MealOrderRepository mealOrderRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private OutboxPublisher outboxPublisher;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderSagaKafkaConsumer sagaConsumer;

    private MealOrder pendingOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = MealOrder.builder()
                .id(100L)
                .userId(10L)
                .statut(OrderStatus.RECU)
                .montantTotal(new BigDecimal("18.50"))
                .date(LocalDateTime.now())
                .idempotencyKey(UUID.randomUUID())
                .correlationId("corr-123")
                .build();
    }

    @Test
    void shouldAdvanceToPaymentWhenIngredientsReserved() throws Exception {
        IngredientsReservedEvent event = IngredientsReservedEvent.builder()
                .eventId("evt-1")
                .correlationId("corr-123")
                .orderId(100L)
                .build();
        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-1")).thenReturn(false);
        when(mealOrderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        sagaConsumer.onIngredientsReserved(json);

        verify(outboxPublisher, times(1)).enqueue(
                eq("MealOrder"),
                eq("100"),
                eq("MealPaymentRequestedEvent"),
                eq(KafkaTopics.MEAL_PAYMENT_REQUESTED),
                any(MealPaymentRequestedEvent.class)
        );
        verify(processedEventRepository, times(1)).save(any());
    }

    @Test
    void shouldCancelOrderWhenIngredientsRejected() throws Exception {
        IngredientsRejectedEvent event = IngredientsRejectedEvent.builder()
                .eventId("evt-2")
                .correlationId("corr-123")
                .orderId(100L)
                .reason("Rupture de saumon")
                .build();
        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-2")).thenReturn(false);
        when(mealOrderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        sagaConsumer.onIngredientsRejected(json);

        assertEquals(OrderStatus.ANNULE, pendingOrder.getStatut());
        verify(mealOrderRepository, times(1)).save(pendingOrder);
        verify(outboxPublisher, times(1)).enqueue(
                eq("MealOrder"),
                eq("100"),
                eq("OrderCancelledEvent"),
                eq(KafkaTopics.ORDER_CANCELLED),
                any(OrderCancelledEvent.class)
        );
    }

    @Test
    void shouldConfirmOrderWhenPaymentSucceeded() throws Exception {
        MealPaymentSucceededEvent event = MealPaymentSucceededEvent.builder()
                .eventId("evt-3")
                .correlationId("corr-123")
                .orderId(100L)
                .paymentId(55L)
                .userId(10L)
                .amount(new BigDecimal("18.50"))
                .build();
        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-3")).thenReturn(false);
        when(mealOrderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        sagaConsumer.onMealPaymentSucceeded(json);

        assertEquals(OrderStatus.EN_PREPARATION, pendingOrder.getStatut());
        verify(mealOrderRepository, times(1)).save(pendingOrder);
        verify(outboxPublisher, times(1)).enqueue(
                eq("MealOrder"),
                eq("100"),
                eq("OrderPlacedEvent"),
                eq(KafkaTopics.ORDER_PLACED),
                any(OrderPlacedEvent.class)
        );
    }

    @Test
    void shouldCancelOrderWhenPaymentFailed() throws Exception {
        MealPaymentFailedEvent event = MealPaymentFailedEvent.builder()
                .eventId("evt-4")
                .correlationId("corr-123")
                .orderId(100L)
                .userId(10L)
                .amount(new BigDecimal("18.50"))
                .reason("Solde insuffisant")
                .build();
        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-4")).thenReturn(false);
        when(mealOrderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        sagaConsumer.onMealPaymentFailed(json);

        assertEquals(OrderStatus.ANNULE, pendingOrder.getStatut());
        verify(mealOrderRepository, times(1)).save(pendingOrder);
        verify(outboxPublisher, times(1)).enqueue(
                eq("MealOrder"),
                eq("100"),
                eq("OrderCancelledEvent"),
                eq(KafkaTopics.ORDER_CANCELLED),
                any(OrderCancelledEvent.class)
        );
    }

    @Test
    void shouldSkipDuplicateEvent() throws Exception {
        IngredientsReservedEvent event = IngredientsReservedEvent.builder()
                .eventId("evt-dup")
                .correlationId("corr-123")
                .orderId(100L)
                .build();
        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-dup")).thenReturn(true);

        sagaConsumer.onIngredientsReserved(json);

        verify(mealOrderRepository, never()).findById(anyLong());
        verify(outboxPublisher, never()).enqueue(any(), any(), any(), any(), any());
    }
}

