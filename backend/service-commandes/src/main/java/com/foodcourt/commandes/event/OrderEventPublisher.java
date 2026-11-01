package com.foodcourt.commandes.event;

import com.foodcourt.commandes.config.RabbitMQConfig;
import com.foodcourt.commandes.entity.MealOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderReady(MealOrder order, List<String> dishNames) {
        OrderReadyEvent event = OrderReadyEvent.builder()
                .eventId(UUID.randomUUID())
                .orderId(order.getId())
                .userId(order.getUserId())
                .timestamp(LocalDateTime.now())
                .montantTotal(order.getMontantTotal())
                .dishNames(dishNames)
                .build();

        log.info("Publishing OrderReadyEvent to RabbitMQ: orderId={}, eventId={}", order.getId(), event.getEventId());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_ORDER_READY,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish OrderReadyEvent to RabbitMQ: {}", e.getMessage());
        }
    }

    public void publishOrderCancelled(MealOrder order, String reason) {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID())
                .orderId(order.getId())
                .userId(order.getUserId())
                .timestamp(LocalDateTime.now())
                .refundedAmount(order.getMontantTotal())
                .reason(reason)
                .build();

        log.info("Publishing OrderCancelledEvent to RabbitMQ: orderId={}, eventId={}", order.getId(), event.getEventId());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_ORDER_CANCELLED,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish OrderCancelledEvent to RabbitMQ: {}", e.getMessage());
        }
    }
}

