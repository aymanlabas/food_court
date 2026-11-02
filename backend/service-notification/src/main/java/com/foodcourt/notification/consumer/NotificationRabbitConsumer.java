package com.foodcourt.notification.consumer;

import com.foodcourt.notification.config.RabbitMQConfig;
import com.foodcourt.notification.event.OrderCancelledEvent;
import com.foodcourt.notification.event.OrderReadyEvent;
import com.foodcourt.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATIONS)
@RequiredArgsConstructor
@Slf4j
public class NotificationRabbitConsumer {

    private final NotificationService notificationService;

    @RabbitHandler
    public void handleOrderReady(OrderReadyEvent event) {
        log.info("Received OrderReadyEvent via RabbitMQ for order ID: {}", event.getOrderId());
        notificationService.processOrderReady(event);
    }

    @RabbitHandler
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent via RabbitMQ for order ID: {}", event.getOrderId());
        notificationService.processOrderCancelled(event);
    }
}

