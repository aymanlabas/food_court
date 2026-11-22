package com.foodcourt.notification.messaging;

import com.foodcourt.notification.config.RabbitMQConfig;
import com.foodcourt.notification.dtos.NotificationRequest;
import com.foodcourt.notification.services.EmailService;
import com.foodcourt.notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeOrderEvent(NotificationRequest request) {
        log.info("Message reçu depuis RabbitMQ : {}", request);
        notificationService.create(request);
        emailService.sendNotificationEmail(request);
    }
}