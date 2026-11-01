package com.foodcourt.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "foodcourt.exchange";
    public static final String QUEUE_NOTIFICATIONS = "order.notifications.queue";
    public static final String ROUTING_KEY_ORDER_READY = "order.ready";
    public static final String ROUTING_KEY_ORDER_CANCELLED = "order.cancelled";

    @Bean
    public TopicExchange foodCourtExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATIONS).build();
    }

    @Bean
    public Binding bindingOrderReady(Queue notificationsQueue, TopicExchange foodCourtExchange) {
        return BindingBuilder.bind(notificationsQueue).to(foodCourtExchange).with(ROUTING_KEY_ORDER_READY);
    }

    @Bean
    public Binding bindingOrderCancelled(Queue notificationsQueue, TopicExchange foodCourtExchange) {
        return BindingBuilder.bind(notificationsQueue).to(foodCourtExchange).with(ROUTING_KEY_ORDER_CANCELLED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
