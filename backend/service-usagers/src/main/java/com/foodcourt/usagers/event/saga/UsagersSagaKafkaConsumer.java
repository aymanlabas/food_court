package com.foodcourt.usagers.event.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcourt.usagers.config.KafkaTopics;
import com.foodcourt.usagers.entity.Payment;
import com.foodcourt.usagers.entity.PaymentStatus;
import com.foodcourt.usagers.entity.User;
import com.foodcourt.usagers.outbox.OutboxPublisher;
import com.foodcourt.usagers.outbox.ProcessedEvent;
import com.foodcourt.usagers.outbox.ProcessedEventRepository;
import com.foodcourt.usagers.repository.PaymentRepository;
import com.foodcourt.usagers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsagersSagaKafkaConsumer {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final OutboxPublisher outboxPublisher;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Consumes meal-payment-requested to debit cafeteria badge balance in a local transaction.
     * Emits meal-payment-succeeded or meal-payment-failed.
     */
    @KafkaListener(topics = KafkaTopics.MEAL_PAYMENT_REQUESTED, groupId = "service-usagers-group")
    @Transactional
    public void onMealPaymentRequested(String message) {
        try {
            MealPaymentRequestedEvent event = objectMapper.readValue(message, MealPaymentRequestedEvent.class);
            log.info("Received MealPaymentRequestedEvent: orderId={}, userId={}, amount={}",
                    event.getOrderId(), event.getUserId(), event.getAmount());

            if (isEventProcessed(event.getEventId())) {
                log.info("Event {} already processed, skipping", event.getEventId());
                return;
            }

            Optional<User> userOpt = userRepository.findById(event.getUserId());
            if (userOpt.isEmpty()) {
                log.warn("User {} not found for payment request", event.getUserId());
                emitPaymentFailed(event, "Collaborateur non trouvé");
                markEventProcessed(event.getEventId(), "MealPaymentRequestedEvent");
                return;
            }

            User user = userOpt.get();
            if (user.getSoldeCantine().compareTo(event.getAmount()) < 0) {
                log.warn("Insufficient badge balance for user {}. Required: {}, Available: {}",
                        user.getId(), event.getAmount(), user.getSoldeCantine());

                Payment failedPayment = Payment.builder()
                        .userId(user.getId())
                        .mealOrderId(event.getOrderId())
                        .montant(event.getAmount())
                        .moyen("BADGE")
                        .statut(PaymentStatus.ECHOUE)
                        .idempotencyKey(UUID.randomUUID())
                        .build();
                paymentRepository.save(failedPayment);

                emitPaymentFailed(event, "Solde badge insuffisant (" + user.getSoldeCantine() + " € disponible)");
                markEventProcessed(event.getEventId(), "MealPaymentRequestedEvent");
                return;
            }

            // Debit badge balance in local transaction
            user.setSoldeCantine(user.getSoldeCantine().subtract(event.getAmount()));
            userRepository.save(user);

            Payment payment = Payment.builder()
                    .userId(user.getId())
                    .mealOrderId(event.getOrderId())
                    .montant(event.getAmount())
                    .moyen("BADGE")
                    .statut(PaymentStatus.VALIDE)
                    .idempotencyKey(UUID.randomUUID())
                    .build();
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment {} debited successfully for order {}", savedPayment.getId(), event.getOrderId());

            MealPaymentSucceededEvent succeededEvent = MealPaymentSucceededEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .correlationId(event.getCorrelationId())
                    .orderId(event.getOrderId())
                    .paymentId(savedPayment.getId())
                    .userId(user.getId())
                    .amount(event.getAmount())
                    .timestamp(Instant.now())
                    .build();

            outboxPublisher.enqueue("Payment", savedPayment.getId().toString(),
                    "MealPaymentSucceededEvent", KafkaTopics.MEAL_PAYMENT_SUCCEEDED, succeededEvent);

            markEventProcessed(event.getEventId(), "MealPaymentRequestedEvent");
        } catch (Exception e) {
            log.error("Error processing MealPaymentRequestedEvent: {}", message, e);
            throw new RuntimeException("Failed to process payment requested event", e);
        }
    }

    /**
     * Consumes order-cancelled to refund any completed payment.
     */
    @KafkaListener(topics = KafkaTopics.ORDER_CANCELLED, groupId = "service-usagers-group")
    @Transactional
    public void onOrderCancelled(String message) {
        try {
            OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);
            log.info("Received OrderCancelledEvent in service-usagers: orderId={}", event.getOrderId());

            if (isEventProcessed(event.getEventId())) {
                log.info("Event {} already processed, skipping", event.getEventId());
                return;
            }

            Optional<Payment> paymentOpt = paymentRepository.findByMealOrderId(event.getOrderId());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if (payment.getStatut() == PaymentStatus.VALIDE) {
                    Optional<User> userOpt = userRepository.findById(payment.getUserId());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        user.setSoldeCantine(user.getSoldeCantine().add(payment.getMontant()));
                        userRepository.save(user);

                        payment.setStatut(PaymentStatus.REMBOURSE);
                        paymentRepository.save(payment);

                        log.info("Refunded {} € to user {} for cancelled order {}",
                                payment.getMontant(), user.getId(), event.getOrderId());
                    }
                }
            }

            markEventProcessed(event.getEventId(), "OrderCancelledEvent");
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent in usagers: {}", message, e);
            throw new RuntimeException("Failed to process order cancelled event in usagers", e);
        }
    }

    private void emitPaymentFailed(MealPaymentRequestedEvent event, String reason) {
        MealPaymentFailedEvent failedEvent = MealPaymentFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .correlationId(event.getCorrelationId())
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(event.getAmount())
                .reason(reason)
                .timestamp(Instant.now())
                .build();

        outboxPublisher.enqueue("Payment", event.getOrderId().toString(),
                "MealPaymentFailedEvent", KafkaTopics.MEAL_PAYMENT_FAILED, failedEvent);
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
