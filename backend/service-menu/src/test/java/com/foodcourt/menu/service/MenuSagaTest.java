package com.foodcourt.menu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcourt.menu.config.KafkaTopics;
import com.foodcourt.menu.dto.IngredientReservationResponse;
import com.foodcourt.menu.event.saga.*;
import com.foodcourt.menu.outbox.OutboxPublisher;
import com.foodcourt.menu.outbox.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuSagaTest {

    @Mock
    private StockService stockService;

    @Mock
    private OutboxPublisher outboxPublisher;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MenuSagaKafkaConsumer menuSagaConsumer;

    @Test
    void shouldReserveIngredientsAndEmitEvent() throws Exception {
        OrderItemPayload item = OrderItemPayload.builder()
                .dishId(1L)
                .dishName("Poke Bowl")
                .quantity(2)
                .unitPrice(new BigDecimal("12.50"))
                .build();

        MealOrderRequestedEvent event = MealOrderRequestedEvent.builder()
                .eventId("evt-menu-1")
                .correlationId("corr-menu-1")
                .orderId(101L)
                .userId(1L)
                .items(List.of(item))
                .totalAmount(new BigDecimal("25.00"))
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-menu-1")).thenReturn(false);
        when(stockService.reserveIngredients(any())).thenReturn(IngredientReservationResponse.builder()
                .success(true)
                .message("OK")
                .build());

        menuSagaConsumer.onMealOrderRequested(json);

        verify(outboxPublisher, times(1)).enqueue(
                eq("MenuOrder"),
                eq("101"),
                eq("IngredientsReservedEvent"),
                eq(KafkaTopics.INGREDIENTS_RESERVED),
                any(IngredientsReservedEvent.class)
        );
        verify(processedEventRepository, times(1)).save(any());
    }

    @Test
    void shouldEmitIngredientsRejectedWhenStockInsufficient() throws Exception {
        OrderItemPayload item = OrderItemPayload.builder()
                .dishId(1L)
                .quantity(5)
                .build();

        MealOrderRequestedEvent event = MealOrderRequestedEvent.builder()
                .eventId("evt-menu-2")
                .correlationId("corr-menu-2")
                .orderId(102L)
                .userId(1L)
                .items(List.of(item))
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-menu-2")).thenReturn(false);
        when(stockService.reserveIngredients(any())).thenReturn(IngredientReservationResponse.builder()
                .success(false)
                .insufficientIngredients(List.of("Saumon Frais"))
                .message("Stock insuffisant")
                .build());

        menuSagaConsumer.onMealOrderRequested(json);

        verify(outboxPublisher, times(1)).enqueue(
                eq("MenuOrder"),
                eq("102"),
                eq("IngredientsRejectedEvent"),
                eq(KafkaTopics.INGREDIENTS_REJECTED),
                any(IngredientsRejectedEvent.class)
        );
    }
}

