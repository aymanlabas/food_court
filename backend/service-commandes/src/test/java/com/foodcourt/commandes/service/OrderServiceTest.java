package com.foodcourt.commandes.service;

import com.foodcourt.commandes.dto.*;
import com.foodcourt.commandes.entity.MealOrder;
import com.foodcourt.commandes.entity.OrderStatus;
import com.foodcourt.commandes.exception.*;
import com.foodcourt.commandes.feign.MenuServiceClient;
import com.foodcourt.commandes.feign.UsagerServiceClient;
import com.foodcourt.commandes.feign.dto.*;
import com.foodcourt.commandes.mapper.OrderMapper;
import com.foodcourt.commandes.repository.MealOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private MealOrderRepository mealOrderRepository;

    @Mock
    private MenuServiceClient menuServiceClient;

    @Mock
    private UsagerServiceClient usagerServiceClient;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private UUID idempotencyKey;
    private UserSummaryResponse userSummary;
    private DishValidationResponse dishValidation;
    private IngredientReservationResponse stockAvailable;
    private CreateOrderRequest createRequest;
    private MealOrder savedOrder;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        idempotencyKey = UUID.randomUUID();

        userSummary = UserSummaryResponse.builder()
                .id(1L)
                .nom("Alice Dupont")
                .email("alice@foodcourt.com")
                .soldeCantine(new BigDecimal("50.00"))
                .build();

        dishValidation = DishValidationResponse.builder()
                .dishId(10L)
                .nom("Salade César")
                .prix(new BigDecimal("12.00"))
                .disponible(true)
                .valid(true)
                .build();

        stockAvailable = IngredientReservationResponse.builder()
                .success(true)
                .message("OK")
                .build();

        createRequest = CreateOrderRequest.builder()
                .userId(1L)
                .idempotencyKey(idempotencyKey)
                .items(List.of(new OrderItemRequest(10L, 2)))
                .build();

        savedOrder = MealOrder.builder()
                .id(100L)
                .userId(1L)
                .date(LocalDateTime.now())
                .montantTotal(new BigDecimal("24.00"))
                .statut(OrderStatus.RECU)
                .idempotencyKey(idempotencyKey)
                .items(new ArrayList<>())
                .build();

        orderResponse = OrderResponse.builder()
                .id(100L)
                .userId(1L)
                .montantTotal(new BigDecimal("24.00"))
                .statut(OrderStatus.EN_PREPARATION)
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        when(mealOrderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(usagerServiceClient.getUserById(1L)).thenReturn(userSummary);
        when(menuServiceClient.validateDish(10L)).thenReturn(dishValidation);
        when(usagerServiceClient.getUserBalance(1L)).thenReturn(new BigDecimal("50.00"));
        when(menuServiceClient.checkDishesAvailability(any())).thenReturn(stockAvailable);
        when(mealOrderRepository.save(any(MealOrder.class))).thenReturn(savedOrder);
        when(usagerServiceClient.debitBadge(any())).thenReturn(new DebitResponse(1L, "CONFIRME"));
        when(orderMapper.toResponse(any())).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(createRequest);

        assertNotNull(result);
        assertEquals(OrderStatus.EN_PREPARATION, result.getStatut());
        verify(usagerServiceClient, times(1)).debitBadge(any());
    }

    @Test
    void shouldFailWhenBadgeBalanceIsInsufficient() {
        when(mealOrderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(usagerServiceClient.getUserById(1L)).thenReturn(userSummary);
        when(menuServiceClient.validateDish(10L)).thenReturn(dishValidation);
        when(usagerServiceClient.getUserBalance(1L)).thenReturn(new BigDecimal("10.00")); // Required 24.00

        assertThrows(PaymentFailedException.class, () -> orderService.createOrder(createRequest));
        verify(menuServiceClient, never()).checkDishesAvailability(any());
        verify(usagerServiceClient, never()).debitBadge(any());
    }

    @Test
    void shouldFailWhenDishIsUnavailable() {
        dishValidation.setDisponible(false);

        when(mealOrderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(usagerServiceClient.getUserById(1L)).thenReturn(userSummary);
        when(menuServiceClient.validateDish(10L)).thenReturn(dishValidation);

        assertThrows(InvalidOrderException.class, () -> orderService.createOrder(createRequest));
        verify(usagerServiceClient, never()).debitBadge(any());
    }

    @Test
    void shouldCompensateWhenPaymentFails() {
        when(mealOrderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(usagerServiceClient.getUserById(1L)).thenReturn(userSummary);
        when(menuServiceClient.validateDish(10L)).thenReturn(dishValidation);
        when(usagerServiceClient.getUserBalance(1L)).thenReturn(new BigDecimal("50.00"));
        when(menuServiceClient.checkDishesAvailability(any())).thenReturn(stockAvailable);
        when(mealOrderRepository.save(any(MealOrder.class))).thenReturn(savedOrder);
        when(usagerServiceClient.debitBadge(any())).thenThrow(new RuntimeException("Paiement rejeté"));

        assertThrows(PaymentFailedException.class, () -> orderService.createOrder(createRequest));
        assertEquals(OrderStatus.ANNULE, savedOrder.getStatut());
    }

    @Test
    void shouldTransitionStatusCorrectly() {
        savedOrder.setStatut(OrderStatus.EN_PREPARATION);
        when(mealOrderRepository.findById(100L)).thenReturn(Optional.of(savedOrder));
        when(mealOrderRepository.save(savedOrder)).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse res = orderService.updateOrderStatus(100L, OrderStatus.PRET);

        assertNotNull(res);
        assertEquals(OrderStatus.PRET, savedOrder.getStatut());
    }

    @Test
    void shouldForbidTransitionWhenOrderIsAlreadyServed() {
        savedOrder.setStatut(OrderStatus.SERVI);
        when(mealOrderRepository.findById(100L)).thenReturn(Optional.of(savedOrder));

        assertThrows(InvalidStatusTransitionException.class, () ->
                orderService.updateOrderStatus(100L, OrderStatus.EN_PREPARATION));
    }

    @Test
    void shouldForbidCancellationWhenOrderIsAlreadyServed() {
        savedOrder.setStatut(OrderStatus.SERVI);
        when(mealOrderRepository.findById(100L)).thenReturn(Optional.of(savedOrder));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.cancelOrder(100L));
    }

    @Test
    void shouldCancelAndRefundActiveOrder() {
        savedOrder.setStatut(OrderStatus.EN_PREPARATION);
        when(mealOrderRepository.findById(100L)).thenReturn(Optional.of(savedOrder));
        when(mealOrderRepository.save(savedOrder)).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(orderResponse);

        OrderResponse res = orderService.cancelOrder(100L);

        assertNotNull(res);
        assertEquals(OrderStatus.ANNULE, savedOrder.getStatut());
        verify(usagerServiceClient, times(1)).refundPayment(any());
    }
}

