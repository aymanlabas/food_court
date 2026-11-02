package com.foodcourt.usagers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodcourt.usagers.config.KafkaTopics;
import com.foodcourt.usagers.entity.Payment;
import com.foodcourt.usagers.entity.User;
import com.foodcourt.usagers.event.saga.*;
import com.foodcourt.usagers.outbox.OutboxPublisher;
import com.foodcourt.usagers.outbox.ProcessedEventRepository;
import com.foodcourt.usagers.repository.PaymentRepository;
import com.foodcourt.usagers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsagersSagaTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OutboxPublisher outboxPublisher;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private UsagersSagaKafkaConsumer usagersSagaConsumer;

    private User employee;

    @BeforeEach
    void setUp() {
        employee = User.builder()
                .id(1L)
                .nom("Alice Dupont")
                .email("alice@foodcourt.com")
                .soldeCantine(new BigDecimal("50.00"))
                .build();
    }

    @Test
    void shouldDebitBadgeAndEmitSucceededEvent() throws Exception {
        MealPaymentRequestedEvent event = MealPaymentRequestedEvent.builder()
                .eventId("evt-pay-1")
                .correlationId("corr-pay-1")
                .orderId(200L)
                .userId(1L)
                .amount(new BigDecimal("20.00"))
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-pay-1")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(999L);
            return p;
        });

        usagersSagaConsumer.onMealPaymentRequested(json);

        assertEquals(new BigDecimal("30.00"), employee.getSoldeCantine());
        verify(userRepository, times(1)).save(employee);
        verify(outboxPublisher, times(1)).enqueue(
                eq("Payment"),
                eq("999"),
                eq("MealPaymentSucceededEvent"),
                eq(KafkaTopics.MEAL_PAYMENT_SUCCEEDED),
                any(MealPaymentSucceededEvent.class)
        );
    }

    @Test
    void shouldEmitPaymentFailedWhenBalanceInsufficient() throws Exception {
        MealPaymentRequestedEvent event = MealPaymentRequestedEvent.builder()
                .eventId("evt-pay-2")
                .correlationId("corr-pay-2")
                .orderId(201L)
                .userId(1L)
                .amount(new BigDecimal("100.00"))
                .build();

        String json = objectMapper.writeValueAsString(event);

        when(processedEventRepository.existsById("evt-pay-2")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));

        usagersSagaConsumer.onMealPaymentRequested(json);

        assertEquals(new BigDecimal("50.00"), employee.getSoldeCantine());
        verify(outboxPublisher, times(1)).enqueue(
                eq("Payment"),
                eq("201"),
                eq("MealPaymentFailedEvent"),
                eq(KafkaTopics.MEAL_PAYMENT_FAILED),
                any(MealPaymentFailedEvent.class)
        );
    }
}

