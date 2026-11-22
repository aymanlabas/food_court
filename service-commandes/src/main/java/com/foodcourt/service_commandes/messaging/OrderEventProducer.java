package com.foodcourt.service_commandes.messaging;

import com.foodcourt.service_commandes.config.RabbitMQConfig;
import com.foodcourt.service_commandes.dtos.OrderNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderEventAfterCommit(OrderNotificationEvent event) {
        String routingKey = "order." + event.type().toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
        log.info("Événement publié dans RabbitMQ [{}] : {}", routingKey, event);
    }
}