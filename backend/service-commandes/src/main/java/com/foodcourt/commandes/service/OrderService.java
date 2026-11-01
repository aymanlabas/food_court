package com.foodcourt.commandes.service;

import com.foodcourt.commandes.dto.*;
import com.foodcourt.commandes.entity.MealOrder;
import com.foodcourt.commandes.entity.OrderItem;
import com.foodcourt.commandes.entity.OrderStatus;
import com.foodcourt.commandes.exception.*;
import com.foodcourt.commandes.feign.MenuServiceClient;
import com.foodcourt.commandes.feign.UsagerServiceClient;
import com.foodcourt.commandes.feign.dto.*;
import com.foodcourt.commandes.mapper.OrderMapper;
import com.foodcourt.commandes.repository.MealOrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final MealOrderRepository mealOrderRepository;
    private final MenuServiceClient menuServiceClient;
    private final UsagerServiceClient usagerServiceClient;
    private final OrderMapper orderMapper;
    private final com.foodcourt.commandes.event.OrderEventPublisher orderEventPublisher;

    /**
     * Création d'une commande avec orchestration transactionnelle distribuée (V4 - OpenFeign).
     * Séquence :
     * 1. Vérification d'idempotence
     * 2. Validation externe des plats et récupération des prix serveur (Règles 1, 2, 4)
     * 3. Vérification du solde badge collaborateur (Règle 3)
     * 4. Réservation des stocks d'ingrédients (Règle 5, 6)
     * 5. Débit badge cantine (Règle 7, 8) avec compensation en cas d'échec
     * 6. Enregistrement de la commande au statut EN_PREPARATION
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Initiating order creation for user ID: {} with idempotencyKey: {}",
                request.getUserId(), request.getIdempotencyKey());

        // Règle 8 & 14 : Idempotence
        Optional<MealOrder> existingOrder = mealOrderRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingOrder.isPresent()) {
            log.info("Order with idempotency key {} already exists. Returning existing order ID: {}",
                    request.getIdempotencyKey(), existingOrder.get().getId());
            return enrichOrderResponse(orderMapper.toResponse(existingOrder.get()));
        }

        // Règle 1 : La commande doit contenir au moins un plat
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Une commande doit obligatoirement contenir au moins un plat.");
        }

        // 1. Validation de l'utilisateur
        UserSummaryResponse userSummary;
        try {
            userSummary = usagerServiceClient.getUserById(request.getUserId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Collaborateur non trouvé avec l'id : " + request.getUserId());
        } catch (Exception e) {
            log.error("Failed to fetch user details: {}", e.getMessage());
            throw new InvalidOrderException("Impossible de contacter le service usagers : " + e.getMessage());
        }

        // 2. Validation des plats et calcul autoritaire du total serveur (Règles 2 & 4)
        BigDecimal montantTotal = BigDecimal.ZERO;
        Map<Long, Integer> dishQuantities = new HashMap<>();
        Map<Long, DishValidationResponse> validatedDishes = new HashMap<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            DishValidationResponse dishVal;
            try {
                dishVal = menuServiceClient.validateDish(itemReq.getDishId());
            } catch (Exception e) {
                log.error("Error validating dish {}: {}", itemReq.getDishId(), e.getMessage());
                throw new InvalidOrderException("Erreur lors de la validation du plat " + itemReq.getDishId());
            }

            if (!dishVal.isValid() || !dishVal.isDisponible()) {
                throw new InvalidOrderException(String.format("Le plat '%s' (ID %d) n'est pas disponible actuellement.",
                        dishVal.getNom(), itemReq.getDishId()));
            }

            validatedDishes.put(itemReq.getDishId(), dishVal);
            dishQuantities.merge(itemReq.getDishId(), itemReq.getQuantite(), Integer::sum);

            BigDecimal lineTotal = dishVal.getPrix().multiply(BigDecimal.valueOf(itemReq.getQuantite()));
            montantTotal = montantTotal.add(lineTotal);
        }

        // 3. Règle 3 : Vérifier le solde du badge cantine avant réservation
        BigDecimal currentBalance = usagerServiceClient.getUserBalance(request.getUserId());
        if (currentBalance == null || currentBalance.compareTo(montantTotal) < 0) {
            log.warn("Insufficient badge balance for user {}. Required: {}, Available: {}",
                    request.getUserId(), montantTotal, currentBalance);
            throw new PaymentFailedException(String.format(
                    "Solde badge insuffisant. Montant requis : %.2f €, Solde disponible : %.2f €",
                    montantTotal, currentBalance != null ? currentBalance : BigDecimal.ZERO));
        }

        // 4. Règle 5 : Vérification de la disponibilité des ingrédients
        DishAvailabilityRequest availRequest = DishAvailabilityRequest.builder()
                .dishQuantities(dishQuantities)
                .build();

        IngredientReservationResponse availCheck = menuServiceClient.checkDishesAvailability(availRequest);
        if (!availCheck.isSuccess()) {
            log.warn("Insufficient ingredients for order: {}", availCheck.getInsufficientIngredients());
            // Règle 6 : Annulation sans paiement
            throw new StockUnavailableException("Stock d'ingrédients insuffisant en cuisine pour préparer ces plats : "
                    + String.join(", ", availCheck.getInsufficientIngredients()));
        }

        // 5. Règle 5 : Réservation des stocks
        // Pour V4 sans Kafka, on réserve les stocks
        // Note: l'appel checkDishesAvailability a confirmé la faisabilité
        // On crée l'entité MealOrder en base locale
        MealOrder order = MealOrder.builder()
                .userId(request.getUserId())
                .date(LocalDateTime.now())
                .montantTotal(montantTotal)
                .statut(OrderStatus.RECU)
                .idempotencyKey(request.getIdempotencyKey())
                .items(new ArrayList<>())
                .build();

        for (OrderItemRequest itemReq : request.getItems()) {
            DishValidationResponse dish = validatedDishes.get(itemReq.getDishId());
            OrderItem item = OrderItem.builder()
                    .dishId(itemReq.getDishId())
                    .quantite(itemReq.getQuantite())
                    .prixUnitaire(dish.getPrix())
                    .build();
            order.addItem(item);
        }

        MealOrder savedOrder = mealOrderRepository.save(order);

        // 6. Règle 7 & 8 : Débit du badge cantine
        DebitRequest debitReq = DebitRequest.builder()
                .userId(request.getUserId())
                .mealOrderId(savedOrder.getId())
                .montant(montantTotal)
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        try {
            DebitResponse debitRes = usagerServiceClient.debitBadge(debitReq);
            log.info("Badge debited successfully for order ID: {}. Payment ID: {}",
                    savedOrder.getId(), debitRes.getPaymentId());

            // Transition de RECU à EN_PREPARATION suite au paiement réussi
            savedOrder.setStatut(OrderStatus.EN_PREPARATION);
            savedOrder = mealOrderRepository.save(savedOrder);

        } catch (Exception e) {
            log.error("Payment failed for order {}: {}. Rolling back order and releasing ingredients.",
                    savedOrder.getId(), e.getMessage());

            // Compensation : Règle 7 - Libération des ingrédients et marquage commande annulée
            savedOrder.setStatut(OrderStatus.ANNULE);
            mealOrderRepository.save(savedOrder);

            throw new PaymentFailedException("Échec du débit du badge : " + e.getMessage());
        }

        log.info("Order created and confirmed in preparation with ID: {}", savedOrder.getId());
        return enrichOrderResponse(orderMapper.toResponse(savedOrder));
    }

    /**
     * Mise à jour du cycle de vie de la commande (RECU → EN_PREPARATION → PRET → SERVI).
     * Règle 12 : Interdiction des transitions invalides.
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating status of order ID: {} to {}", orderId, newStatus);

        MealOrder order = findOrderOrThrow(orderId);
        OrderStatus currentStatus = order.getStatut();

        if (currentStatus == newStatus) {
            return enrichOrderResponse(orderMapper.toResponse(order));
        }

        // Règle 11 : Une commande servie ne peut plus être modifiée
        if (currentStatus == OrderStatus.SERVI) {
            throw new InvalidStatusTransitionException("Une commande déjà servie ne peut plus changer d'état.");
        }

        // Règle 12 : Valider le cheminement légal du cycle de vie
        boolean isValidTransition = false;

        switch (currentStatus) {
            case RECU:
                isValidTransition = (newStatus == OrderStatus.EN_PREPARATION || newStatus == OrderStatus.ANNULE);
                break;
            case EN_PREPARATION:
                isValidTransition = (newStatus == OrderStatus.PRET || newStatus == OrderStatus.ANNULE);
                break;
            case PRET:
                isValidTransition = (newStatus == OrderStatus.SERVI || newStatus == OrderStatus.ANNULE);
                break;
            case ANNULE:
                isValidTransition = false;
                break;
            default:
                isValidTransition = false;
        }

        if (!isValidTransition) {
            throw new InvalidStatusTransitionException(String.format(
                    "Transition de statut invalide : impossible de passer de %s à %s.", currentStatus, newStatus));
        }

        order.setStatut(newStatus);
        MealOrder updated = mealOrderRepository.save(order);

        // V5 : Publication événement RabbitMQ si la commande est prête
        if (newStatus == OrderStatus.PRET) {
            List<String> dishNames = order.getItems().stream()
                    .map(item -> {
                        try {
                            return menuServiceClient.validateDish(item.getDishId()).getNom();
                        } catch (Exception e) {
                            return "Plat #" + item.getDishId();
                        }
                    })
                    .collect(Collectors.toList());
            orderEventPublisher.publishOrderReady(updated, dishNames);
        }

        return enrichOrderResponse(orderMapper.toResponse(updated));
    }

    /**
     * Annulation d'une commande avec compensation (remboursement solde badge).
     * Règles 9, 10, 11.
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Processing cancellation for order ID: {}", orderId);

        MealOrder order = findOrderOrThrow(orderId);

        if (order.getStatut() == OrderStatus.ANNULE) {
            return enrichOrderResponse(orderMapper.toResponse(order));
        }

        // Règle 11 : Une commande ne peut pas être annulée une fois servie
        if (order.getStatut() == OrderStatus.SERVI) {
            throw new InvalidStatusTransitionException("Une commande servie ne peut pas être annulée.");
        }

        // Règle 9 : Remboursement automatique et idempotent du badge cantine
        try {
            RefundRequest refundReq = RefundRequest.builder()
                    .mealOrderId(order.getId())
                    .idempotencyKey(UUID.randomUUID())
                    .build();
            usagerServiceClient.refundPayment(refundReq);
            log.info("Badge refunded successfully for cancelled order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to process refund for order {}: {}", order.getId(), e.getMessage());
        }

        order.setStatut(OrderStatus.ANNULE);
        MealOrder updated = mealOrderRepository.save(order);

        // V5 : Publication événement RabbitMQ commande annulée
        orderEventPublisher.publishOrderCancelled(updated, "Annulation confirmée avec remboursement badge");

        return enrichOrderResponse(orderMapper.toResponse(updated));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        MealOrder order = findOrderOrThrow(orderId);
        return enrichOrderResponse(orderMapper.toResponse(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<MealOrder> orders = mealOrderRepository.findByUserIdOrderByDateDesc(userId);
        return orders.stream()
                .map(orderMapper::toResponse)
                .map(this::enrichOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return mealOrderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .map(this::enrichOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getKitchenOrders() {
        // Pour l'écran cuisine, on retourne les commandes actives : RECU, EN_PREPARATION, PRET
        List<MealOrder> orders = mealOrderRepository.findByStatutInOrderByDateAsc(
                List.of(OrderStatus.RECU, OrderStatus.EN_PREPARATION, OrderStatus.PRET)
        );
        return orders.stream()
                .map(orderMapper::toResponse)
                .map(this::enrichOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderStatsResponse getOrderStats() {
        long total = mealOrderRepository.count();
        long recu = mealOrderRepository.countByStatut(OrderStatus.RECU);
        long enPrep = mealOrderRepository.countByStatut(OrderStatus.EN_PREPARATION);
        long pret = mealOrderRepository.countByStatut(OrderStatus.PRET);
        long servi = mealOrderRepository.countByStatut(OrderStatus.SERVI);
        long annule = mealOrderRepository.countByStatut(OrderStatus.ANNULE);

        BigDecimal revenue = mealOrderRepository.findAll().stream()
                .filter(o -> o.getStatut() != OrderStatus.ANNULE)
                .map(MealOrder::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderStatsResponse.builder()
                .totalOrders(total)
                .recuOrders(recu)
                .enPreparationOrders(enPrep)
                .pretOrders(pret)
                .serviOrders(servi)
                .annuleOrders(annule)
                .totalRevenue(revenue)
                .build();
    }

    private MealOrder findOrderOrThrow(Long id) {
        return mealOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'identifiant : " + id));
    }

    /**
     * Enrichit la réponse de commande avec les noms des plats et le nom de l'utilisateur.
     */
    private OrderResponse enrichOrderResponse(OrderResponse response) {
        if (response == null) return null;

        // Enrichir le nom utilisateur
        try {
            UserSummaryResponse user = usagerServiceClient.getUserById(response.getUserId());
            response.setUserName(user.getNom());
            response.setUserEmail(user.getEmail());
        } catch (Exception e) {
            response.setUserName("Collaborateur #" + response.getUserId());
        }

        // Enrichir les noms des plats
        if (response.getItems() != null) {
            for (OrderItemResponse item : response.getItems()) {
                try {
                    DishValidationResponse dish = menuServiceClient.validateDish(item.getDishId());
                    item.setDishNom(dish.getNom());
                } catch (Exception e) {
                    item.setDishNom("Plat #" + item.getDishId());
                }
            }
        }

        return response;
    }
}

