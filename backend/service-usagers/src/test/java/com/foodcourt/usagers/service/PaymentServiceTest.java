package com.foodcourt.usagers.service;

import com.foodcourt.usagers.dto.DebitRequest;
import com.foodcourt.usagers.dto.DebitResponse;
import com.foodcourt.usagers.entity.Payment;
import com.foodcourt.usagers.entity.PaymentStatus;
import com.foodcourt.usagers.entity.User;
import com.foodcourt.usagers.exception.InsufficientBalanceException;
import com.foodcourt.usagers.mapper.PaymentMapper;
import com.foodcourt.usagers.repository.PaymentRepository;
import com.foodcourt.usagers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        idempotencyKey = UUID.randomUUID();
        user = User.builder()
                .id(1L)
                .nom("Alice Dupont")
                .email("alice@foodcourt.com")
                .soldeCantine(new BigDecimal("50.00"))
                .build();
    }

    @Test
    void shouldDebitBadgeSuccessfullyWhenBalanceIsSufficient() {
        DebitRequest request = DebitRequest.builder()
                .userId(1L)
                .mealOrderId(10L)
                .montant(new BigDecimal("20.00"))
                .idempotencyKey(idempotencyKey)
                .build();

        Payment savedPayment = Payment.builder()
                .id(100L)
                .userId(1L)
                .mealOrderId(10L)
                .montant(new BigDecimal("20.00"))
                .statut(PaymentStatus.CONFIRME)
                .idempotencyKey(idempotencyKey)
                .build();

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        DebitResponse response = paymentService.debitBadge(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.CONFIRME, response.getStatut());
        assertEquals(new BigDecimal("30.00"), user.getSoldeCantine());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenBalanceIsLowerThanAmount() {
        DebitRequest request = DebitRequest.builder()
                .userId(1L)
                .mealOrderId(10L)
                .montant(new BigDecimal("80.00"))
                .idempotencyKey(idempotencyKey)
                .build();

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(InsufficientBalanceException.class, () -> paymentService.debitBadge(request));
        assertEquals(new BigDecimal("50.00"), user.getSoldeCantine());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void shouldReturnExistingPaymentWhenIdempotencyKeyAlreadyExists() {
        DebitRequest request = DebitRequest.builder()
                .userId(1L)
                .mealOrderId(10L)
                .montant(new BigDecimal("20.00"))
                .idempotencyKey(idempotencyKey)
                .build();

        Payment existingPayment = Payment.builder()
                .id(100L)
                .userId(1L)
                .mealOrderId(10L)
                .montant(new BigDecimal("20.00"))
                .statut(PaymentStatus.CONFIRME)
                .idempotencyKey(idempotencyKey)
                .build();

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingPayment));

        DebitResponse response = paymentService.debitBadge(request);

        assertNotNull(response);
        assertEquals(100L, response.getPaymentId());
        assertEquals(PaymentStatus.CONFIRME, response.getStatut());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }
}

