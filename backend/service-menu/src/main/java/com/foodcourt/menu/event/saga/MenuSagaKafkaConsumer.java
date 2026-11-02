package com.foodcourt.menu.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcourt.menu.config.KafkaTopics;
import com.foodcourt.menu.dto.IngredientReservationRequest;
import com.foodcourt.menu.dto.IngredientReservationResponse;
import com.foodcourt.menu.outbox.OutboxPublisher;
import com.foodcourt.menu.outbox.ProcessedEvent;
import com.foodcourt.menu.outbox.ProcessedEventRepository;
import com.foodcourt.menu.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuSagaKafkaConsumer {

    private final StockService stockService;
    private final OutboxPublisher outboxPublisher;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consumes meal-order-requested to reserve required recipe ingredients.
     * Emits ingredients-reserved or ingredients-rejected.
     */
    @KafkaListener(topics = KafkaTopics.MEAL_ORDER_REQUESTED, groupId = "service-menu-group")
    @Transactional
    public void onMealOrderRequested(String message) {
        try {
            MealOrderRequestedEvent event = objectMapper.readValue(message, MealOrderRequestedEvent.class);
            log.info("Received MealOrderRequestedEvent: orderId={}, correlationId={}",
                    event.getOrderId(), event.getCorrelationId());

            if (isEventProcessed(event.getEventId())) {
                log.info("Event {} already processed, skipping", event.getEventId());
                return;
            }

            Map<Long, Integer> dishQuantities = new HashMap<>();
            if (event.getItems() != null) {
                for (OrderItemPayload item : event.getItems()) {
                    dishQuantities.merge(item.getDishId(), item.getQuantity(), Integer::sum);
                }
            }

            Map<Long, BigDecimal> requirements = stockService.calculateRequirements(dishQuantities);
            IngredientReservationRequest reservationReq = IngredientReservationRequest.builder()
                    .ingredientQuantities(requirements)
                    .build();

            IngredientReservationResponse reservationRes = stockService.reserveIngredients(reservationReq);

            if (reservationRes.isSuccess()) {
                log.info("Ingredients reserved successfully for order {}", event.getOrderId());
                IngredientsReservedEvent reservedEvent = IngredientsReservedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .correlationId(event.getCorrelationId())
                        .orderId(event.getOrderId())
                        .timestamp(Instant.now())
                        .build();

                outboxPublisher.enqueue("MenuOrder", event.getOrderId().toString(),
                        "IngredientsReservedEvent", KafkaTopics.INGREDIENTS_RESERVED, reservedEvent);
            } else {
                String reason = reservationRes.getInsufficientIngredients() != null
                        ? String.join(", ", reservationRes.getInsufficientIngredients())
                        : "Ingrédients insuffisants";
                log.warn("Ingredients reservation failed for order {}: {}", event.getOrderId(), reason);

                IngredientsRejectedEvent rejectedEvent = IngredientsRejectedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .correlationId(event.getCorrelationId())
                        .orderId(event.getOrderId())
                        .reason(reason)
                        .timestamp(Instant.now())
                        .build();

                outboxPublisher.enqueue("MenuOrder", event.getOrderId().toString(),
                        "IngredientsRejectedEvent", KafkaTopics.INGREDIENTS_REJECTED, rejectedEvent);
            }

            markEventProcessed(event.getEventId(), "MealOrderRequestedEvent");
        } catch (Exception e) {
            log.error("Error processing MealOrderRequestedEvent: {}", message, e);
            throw new RuntimeException("Failed to process meal order requested event", e);
        }
    }

    /**
     * Consumes order-cancelled to compensate and release reserved ingredients back into stock.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "service-menu-group")
    @Transactional
    public void onOrderCancelled(String message) {
        try {
            OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);
            log.info("Received OrderCancelledEvent in service-menu: orderId={}, correlationId={}",
                    event.getOrderId(), event.getCorrelationId());

            if (isEventProcessed(event.getEventId())) {
                log.info("Event {} already processed, skipping", event.getEventId());
                return;
            }

            // Recalculate dishes availability in case stock changed
            stockService.recalculateDishesAvailability();

            markEventProcessed(event.getEventId(), "OrderCancelledEvent");
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent: {}", message, e);
            throw new RuntimeException("Failed to process order cancelled event in menu", e);
        }
    }

    /**
     * Consumes order-placed to confirm ingredient consumption.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_PLACED, groupId = "service-menu-group")
    @Transactional
    public void onOrderPlaced(String message) {
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            log.info("Received OrderPlacedEvent in service-menu: orderId={}", event.getOrderId());

            if (isEventProcessed(event.getEventId())) {
                return;
            }

            markEventProcessed(event.getEventId(), "OrderPlacedEvent");
        } catch (Exception e) {
            log.error("Error processing OrderPlacedEvent: {}", message, e);
        }
    }

    private boolean isEventProcessed(String eventId) {
        return eventId != null && processedEventRepository.existsById(eventId);
    }

    private void markEventProcessed(String eventId, String eventType) {
        if (eventId != null) {
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .processedAt(Instant.now())
                    .build());
        }
    }
}
