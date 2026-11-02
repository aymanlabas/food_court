package com.foodcourt.commandes.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcourt.commandes.config.KafkaTopics;
import com.foodcourt.commandes.entity.MealOrder;
import com.foodcourt.commandes.entity.OrderStatus;
import com.foodcourt.commandes.outbox.OutboxPublisher;
import com.foodcourt.commandes.outbox.ProcessedEvent;
import com.foodcourt.commandes.outbox.ProcessedEventRepository;
import com.foodcourt.commandes.repository.MealOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaKafkaConsumer {

    private final MealOrderRepository mealOrderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OutboxPublisher outboxPublisher;
    private final ObjectMapper objectMapper;

    /**
     * Handles ingredients reserved event from service-menu.
     * Advances saga: triggers payment request.
     */
    @KafkaListener(topics = KafkaTopics.INGREDIENTS_RESERVED, groupId = "service-commandes-group")
    @Transactional
    public void onIngredientsReserved(String message) {
        try {
            IngredientsReservedEvent event = objectMapper.readValue(message, IngredientsReservedEvent.class);
            log.info("Received IngredientsReservedEvent: orderId={}, correlationId={}", event.getOrderId(), event.getCorrelationId());

            if (isEventProcessed(event.getEventId())) {
                log.info("Event {} already processed, skipping", event.getEventId());
                return;
            }

            MealOrder order = mealOrderRepository.findById(event.getOrderId()).orElse(null);
            if (order == null) {
                log.warn("Order {} not found for ingredients-reserved event", event.getOrderId());
                return;
            }

            if (order.getStatut() == OrderStatus.ANNULE) {
                log.warn("Order {} is already cancelled, skipping payment request", order.getId());
                return;
            }

            // Enqueue payment request
            MealPaymentRequestedEvent paymentRequest = MealPaymentRequestedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .correlationId(event.getCorrelationId())
                    .orderId(order.getId())
                    .userId(order.getUserId())
                    .amount(order.getMontantTotal())
                    .timestamp(Instant.now())
                    .build();

            outboxPublisher.enqueue("MealOrder", order.getId().toString(),
                    "MealPaymentRequestedEvent", KafkaTopics.MEAL_PAYMENT_REQUESTED, paymentRequest);

            markEventProcessed(event.getEventId(), "IngredientsReservedEvent");
        } catch (Exception e) {
            log.error("Error processing IngredientsReservedEvent: {}", message, e);
            throw new RuntimeException("Failed to process ingredients reserved event", e);
        }
    }

    /**
     * Handles ingredients rejected event from service-menu.
     * Cancels the order without payment.
     */
    @KafkaListener(topics = KafkaTopics.INGREDIENTS_REJECTED, groupId = "service-commandes-group")
    @Transactional
    public void onIngredientsRejected(String message) {
        try {
            IngredientsRejectedEvent event = objectMapper.readValue(message, IngredientsRejectedEvent.class);
            log.info("Received IngredientsRejectedEvent: orderId={}, reason={}", event.getOrderId(), event.getReason());

            if (isEventProcessed(event.getEventId())) {
                log.info("Event {} already processed, skipping", event.getEventId());
                return;
            }

            MealOrder order = mealOrderRepository.findById(event.getOrderId()).orElse(null);
            if (order != null && order.getStatut() != OrderStatus.ANNULE) {
                order.setStatut(OrderStatus.ANNULE);
                mealOrderRepository.save(order);

                OrderCancelledEvent cancelEvent = OrderCancelledEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .correlationId(event.getCorrelationId())
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .reason("Stock insuffisant: " + event.getReason())
                        .timestamp(Instant.now())
                        .build();

                outboxPublisher.enqueue("MealOrder", order.getId().toString(),
                        "OrderCancelledEvent", KafkaTopics.ORDER_CANCELLED, cancelEvent);
            }

            markEventProcessed(event.getEventId(), "IngredientsRejectedEvent");
        } catch (Exception e) {
            log.error("Error processing IngredientsRejectedEvent: {}", message, e);
            throw new RuntimeException("Failed to process ingredients rejected event", e);
        }
    }

    /**
     * Handles payment succeeded event from service-usagers.
     * Completes order confirmation and transitions to EN_PREPARATION.
     */
    @KafkaListener(topics = KafkaTopics.MEAL_PAYMENT_SUCCEEDED, groupId = "service-commandes-group")
    @Transactional
    public void onMealPaymentSucceeded(String message) {
        try {
            MealPaymentSucceededEvent event = objectMapper.readValue(message, MealPaymentSucceededEvent.class);
            log.info("Received MealPaymentSucceededEvent: orderId={}, paymentId={}", event.getOrderId(), event.getPaymentId());

            if (isEventProcessed(event.getEventId())) {
                log.info("Event {} already processed, skipping", event.getEventId());
                return;
            }

            MealOrder order = mealOrderRepository.findById(event.getOrderId()).orElse(null);
            if (order != null && order.getStatut() != OrderStatus.ANNULE) {
                order.setStatut(OrderStatus.EN_PREPARATION);
                mealOrderRepository.save(order);

                OrderPlacedEvent placedEvent = OrderPlacedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .correlationId(event.getCorrelationId())
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .totalAmount(order.getMontantTotal())
                        .timestamp(Instant.now())
                        .build();

                outboxPublisher.enqueue("MealOrder", order.getId().toString(),
                        "OrderPlacedEvent", KafkaTopics.ORDER_PLACED, placedEvent);
            }

            markEventProcessed(event.getEventId(), "MealPaymentSucceededEvent");
        } catch (Exception e) {
            log.error("Error processing MealPaymentSucceededEvent: {}", message, e);
            throw new RuntimeException("Failed to process payment succeeded event", e);
        }
    }

    /**
     * Handles payment failed event from service-usagers.
     * Cancels order and triggers compensation to release reserved ingredients.
     */
    @KafkaListener(topics = KafkaTopics.MEAL_PAYMENT_FAILED, groupId = "service-commandes-group")
    @Transactional
    public void onMealPaymentFailed(String message) {
        try {
            MealPaymentFailedEvent event = objectMapper.readValue(message, MealPaymentFailedEvent.class);
            log.info("Received MealPaymentFailedEvent: orderId={}, reason={}", event.getOrderId(), event.getReason());

            if (isEventProcessed(event.getEventId())) {
                log.info("Event {} already processed, skipping", event.getEventId());
                return;
            }

            MealOrder order = mealOrderRepository.findById(event.getOrderId()).orElse(null);
            if (order != null && order.getStatut() != OrderStatus.ANNULE) {
                order.setStatut(OrderStatus.ANNULE);
                mealOrderRepository.save(order);

                // Publish order-cancelled to trigger compensation (release ingredients)
                OrderCancelledEvent cancelEvent = OrderCancelledEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .correlationId(event.getCorrelationId())
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .reason("Paiement échoué: " + event.getReason())
                        .timestamp(Instant.now())
                        .build();

                outboxPublisher.enqueue("MealOrder", order.getId().toString(),
                        "OrderCancelledEvent", KafkaTopics.ORDER_CANCELLED, cancelEvent);
            }

            markEventProcessed(event.getEventId(), "MealPaymentFailedEvent");
        } catch (Exception e) {
            log.error("Error processing MealPaymentFailedEvent: {}", message, e);
            throw new RuntimeException("Failed to process payment failed event", e);
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
