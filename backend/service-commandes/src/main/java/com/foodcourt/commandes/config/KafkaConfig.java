package com.foodcourt.commandes.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:service-commandes-group}")
    private String groupId;

    // ─── TOPICS ───────────────────────────────────────────────
    @Bean
    public NewTopic mealOrderRequestedTopic() {
        return TopicBuilder.name(KafkaTopics.MEAL_ORDER_REQUESTED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic ingredientsReservedTopic() {
        return TopicBuilder.name(KafkaTopics.INGREDIENTS_RESERVED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic ingredientsRejectedTopic() {
        return TopicBuilder.name(KafkaTopics.INGREDIENTS_REJECTED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic mealPaymentRequestedTopic() {
        return TopicBuilder.name(KafkaTopics.MEAL_PAYMENT_REQUESTED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic mealPaymentSucceededTopic() {
        return TopicBuilder.name(KafkaTopics.MEAL_PAYMENT_SUCCEEDED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic mealPaymentFailedTopic() {
        return TopicBuilder.name(KafkaTopics.MEAL_PAYMENT_FAILED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_PLACED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderReadyTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_READY).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CANCELLED).partitions(1).replicas(1).build();
    }

    // ─── PRODUCER CONFIG ──────────────────────────────────────
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ─── CONSUMER & ERROR HANDLER (DLT) ───────────────────────
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            KafkaTemplate<String, String> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Dead letter publishing recoverer sends failed messages after retries to <topic>.DLT
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}

