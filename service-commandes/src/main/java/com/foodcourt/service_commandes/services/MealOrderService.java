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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealOrderService {

    private final MealOrderRepository orderRepository;
    private final ClientFeignClient clientFeignClient;
    private final MenuFeignClient menuFeignClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MealOrderResponseDTO createOrder(MealOrderRequestDTO dto) {

        // ===== V4 OpenFeign : ÉTAPE 1 - Vérifier collaborateur et solde (service-usagers) =====
        ClientDTO client = clientFeignClient.getClientById(dto.userId());
        if (client != null && Boolean.FALSE.equals(client.getActif())) {
            throw new IllegalArgumentException("Le compte client ID " + dto.userId() + " est désactivé.");
        }

        // ===== V4 OpenFeign : ÉTAPE 2 - Vérifier plats et prix (service-menu) =====
        double total = 0.0;
        for (OrderItemDTO item : dto.items()) {
            DishDTO dish = menuFeignClient.getDishById(item.dishId());
            if (dish == null) {
                throw new IllegalArgumentException("Plat introuvable avec ID : " + item.dishId());
            }
            if (Boolean.FALSE.equals(dish.getDisponible())) {
                throw new IllegalArgumentException("Le plat '" + dish.getNom() + "' n'est plus disponible.");
            }
            // Total calculé CÔTÉ SERVEUR depuis le prix du service-menu
            double prixServeur = (dish.getPrix() != null && dish.getPrix() > 0)
                    ? dish.getPrix() : item.prixUnitaire();
            total += prixServeur * item.quantite();
        }

        // ===== V4 OpenFeign : ÉTAPE 3 - Lire les RecipeLines (besoins en ingrédients) =====
        try {
            var recipeLines = menuFeignClient.getAllRecipeLines();
            if (recipeLines != null && !recipeLines.isEmpty()) {
                var dishIds = dto.items().stream().map(OrderItemDTO::dishId).toList();
                var relevantLines = recipeLines.stream()
                        .filter(rl -> dishIds.contains(rl.getDishId()))
                        .toList();
                log.info("RecipeLines récupérées pour les plats commandés : {} ligne(s)", relevantLines.size());
                // En production : vérifier et réserver le stock ici
            }
        } catch (Exception e) {
            log.warn("Impossible de récupérer les RecipeLines depuis service-menu : {}", e.getMessage());
        }

        final double montantFinal = total;

        MealOrder order = MealOrder.builder()
                .userId(dto.userId())
                .date(LocalDateTime.now())
                .montantTotal(montantFinal)
                .statut(OrderStatus.RECU)
                .build();

        List<OrderItem> items = dto.items().stream()
                .map(i -> {
                    DishDTO dish = menuFeignClient.getDishById(i.dishId());
                    double prix = (dish != null && dish.getPrix() != null && dish.getPrix() > 0)
                            ? dish.getPrix() : i.prixUnitaire();
                    return OrderItem.builder()
                            .dishId(i.dishId())
                            .quantite(i.quantite())
                            .prixUnitaire(prix)
                            .mealOrder(order)
                            .build();
                })
                .toList();

        order.setItems(items);
        MealOrder saved = orderRepository.save(order);

        // ===== V5 RabbitMQ : Publier événement COMMANDE_RECUE (AFTER_COMMIT) =====
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

        // ===== V5 RabbitMQ : Événement PRET (repas prêt) ou ANNULE =====
        String notifType = switch (newStatus) {
            case PRET -> "COMMANDE_PRETE";
            case ANNULE -> "COMMANDE_ANNULEE";
            default -> "INFORMATION";
        };

        String message = switch (newStatus) {
            case PRET -> "Votre commande #" + saved.getId() + " est prête ! Venez la récupérer.";
            case ANNULE -> "Votre commande #" + saved.getId() + " a été annulée.";
            default -> "Le statut de votre commande #" + saved.getId() + " est désormais : " + newStatus;
        };

        eventPublisher.publishEvent(new OrderNotificationEvent(
                saved.getUserId(),
                saved.getId(),
                email,
                notifType,
                message
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
