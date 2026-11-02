package com.foodcourt.notification.config;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String ORDER_READY = "order-ready";
    public static final String ORDER_CANCELLED = "order-cancelled";
}

