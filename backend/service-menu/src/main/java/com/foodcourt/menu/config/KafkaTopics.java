package com.foodcourt.menu.config;

public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String MEAL_ORDER_REQUESTED = "meal-order-requested";
    public static final String INGREDIENTS_RESERVED = "ingredients-reserved";
    public static final String INGREDIENTS_REJECTED = "ingredients-rejected";
    public static final String MEAL_PAYMENT_REQUESTED = "meal-payment-requested";
    public static final String MEAL_PAYMENT_SUCCEEDED = "meal-payment-succeeded";
    public static final String MEAL_PAYMENT_FAILED = "meal-payment-failed";
    public static final String ORDER_PLACED = "order-placed";
    public static final String ORDER_READY = "order-ready";
    public static final String ORDER_CANCELLED = "order-cancelled";
}
