package com.foodcourt.service_commandes.services;

import com.foodcourt.service_commandes.clients.ClientFeignClient;
import com.foodcourt.service_commandes.clients.MenuFeignClient;
import com.foodcourt.service_commandes.dtos.MealOrderRequestDTO;
import com.foodcourt.service_commandes.dtos.MealOrderResponseDTO;
import com.foodcourt.service_commandes.dtos.OrderItemDTO;
import com.foodcourt.service_commandes.dtos.OrderNotificationEvent;
import com.foodcourt.service_commandes.dtos.feign.ClientDTO;
import com.foodcourt.service_commandes.dtos.feign.DishDTO;
import com.foodcourt.service_commandes.dtos.feign.OrderEnrichedResponseDTO;
import com.foodcourt.service_commandes.entities.MealOrder;
import com.foodcourt.service_commandes.entities.OrderItem;
import com.foodcourt.service_commandes.entities.OrderStatus;
import com.foodcourt.service_commandes.exceptions.ResourceNotFoundException;
import com.foodcourt.service_commandes.repositories.MealOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealOrderService {

    private final MealOrderRepository orderRepository;
    private final ClientFeignClient clientFeignClient;
    private final MenuFeignClient menuFeignClient;
    private final ApplicationEventPublisher eventPublisher;

    public MealOrderResponseDTO createOrder(MealOrderRequestDTO dto) {
        return createOrder(dto, null);
    }

    @Transactional
    public MealOrderResponseDTO createOrder(MealOrderRequestDTO dto, String idempotencyKey) {
        String normalizedKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey.trim()
                : null;

        if (normalizedKey != null) {
            Optional<MealOrder> existingOrder = orderRepository.findByIdempotencyKey(normalizedKey);
            if (existingOrder.isPresent()) {
                log.info("Rejeu idempotent détecté pour Idempotency-Key='{}' -> retour commande #{}",
                        normalizedKey, existingOrder.get().getId());
                return mapToDTO(existingOrder.get());
            }
        }

        ClientDTO client = clientFeignClient.getClientById(dto.userId());
        if (client != null && Boolean.FALSE.equals(client.getActif())) {
            throw new IllegalArgumentException("Le compte client ID " + dto.userId() + " est désactivé.");
        }

        for (OrderItemDTO item : dto.items()) {
            DishDTO dish = menuFeignClient.getDishById(item.dishId());
            if (dish != null && Boolean.FALSE.equals(dish.getDisponible())) {
                throw new IllegalArgumentException("Le plat '" + dish.getNom() + "' n'est plus disponible.");
            }
        }

        double total = dto.items().stream()
                .mapToDouble(item -> item.prixUnitaire() * item.quantite())
                .sum();

        MealOrder order = MealOrder.builder()
                .userId(dto.userId())
                .date(LocalDateTime.now())
                .montantTotal(total)
                .statut(OrderStatus.RECU)
                .idempotencyKey(normalizedKey)
                .build();

        List<OrderItem> items = dto.items().stream()
                .map(i -> OrderItem.builder()
                        .dishId(i.dishId())
                        .quantite(i.quantite())
                        .prixUnitaire(i.prixUnitaire())
                        .mealOrder(order)
                        .build())
                .toList();

        order.setItems(items);

        MealOrder saved;
        try {
            saved = orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException ex) {
            if (normalizedKey != null) {
                return orderRepository.findByIdempotencyKey(normalizedKey)
                        .map(this::mapToDTO)
                        .orElseThrow(() -> ex);
            }
            throw ex;
        }

        String email = (client != null && client.getEmail() != null)
                ? client.getEmail()
                : "client" + dto.userId() + "@foodcourt.ma";

        eventPublisher.publishEvent(new OrderNotificationEvent(
                saved.getUserId(),
                saved.getId(),
                email,
                "COMMANDE_RECUE",
                "Votre commande #" + saved.getId() + " d'un montant de " + saved.getMontantTotal() + " MAD a bien été reçue."
        ));

        return mapToDTO(saved);
    }

    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return idempotencyKey != null && !idempotencyKey.isBlank()
                && orderRepository.findByIdempotencyKey(idempotencyKey.trim()).isPresent();
    }

    public List<MealOrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public MealOrderResponseDTO getOrderById(Long id) {
        MealOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec ID : " + id));
        return mapToDTO(order);
    }

    public OrderEnrichedResponseDTO getOrderEnrichedById(Long id) {
        MealOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec ID : " + id));

        ClientDTO client = clientFeignClient.getClientById(order.getUserId());

        List<OrderEnrichedResponseDTO.EnrichedItemDTO> enrichedItems = order.getItems().stream()
                .map(it -> {
                    DishDTO dish = menuFeignClient.getDishById(it.getDishId());
                    String nomPlat = (dish != null) ? dish.getNom() : "Plat #" + it.getDishId();
                    return new OrderEnrichedResponseDTO.EnrichedItemDTO(
                            it.getDishId(),
                            nomPlat,
                            it.getQuantite(),
                            it.getPrixUnitaire(),
                            it.getQuantite() * it.getPrixUnitaire()
                    );
                })
                .toList();

        return new OrderEnrichedResponseDTO(
                order.getId(),
                order.getDate(),
                order.getStatut(),
                order.getMontantTotal(),
                client,
                enrichedItems
        );
    }

    @Transactional
    public MealOrderResponseDTO updateStatus(Long id, OrderStatus newStatus) {
        MealOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec ID : " + id));
        order.setStatut(newStatus);
        MealOrder saved = orderRepository.save(order);

        ClientDTO client = clientFeignClient.getClientById(saved.getUserId());
        String email = (client != null && client.getEmail() != null)
                ? client.getEmail()
                : "client" + saved.getUserId() + "@foodcourt.ma";

        String notifType = switch (newStatus.name()) {
            case "PRETE", "PRETE_A_LIVRER", "LIVREE" -> "COMMANDE_PRETE";
            case "ANNULE", "ANNULEE" -> "COMMANDE_ANNULEE";
            default -> "INFORMATION";
        };

        eventPublisher.publishEvent(new OrderNotificationEvent(
                saved.getUserId(),
                saved.getId(),
                email,
                notifType,
                "Le statut de votre commande #" + saved.getId() + " est désormais : " + newStatus
        ));

        return mapToDTO(saved);
    }

    private MealOrderResponseDTO mapToDTO(MealOrder order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(it -> new OrderItemDTO(it.getDishId(), it.getQuantite(), it.getPrixUnitaire()))
                .toList();

        return new MealOrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getDate(),
                order.getMontantTotal(),
                order.getStatut(),
                itemDTOs
        );
    }
}