package com.foodcourt.service_commandes.services;

import com.foodcourt.service_commandes.clients.ClientFeignClient;
import com.foodcourt.service_commandes.clients.MenuFeignClient;
import com.foodcourt.service_commandes.dtos.MealOrderRequestDTO;
import com.foodcourt.service_commandes.dtos.MealOrderResponseDTO;
import com.foodcourt.service_commandes.dtos.OrderItemDTO;
import com.foodcourt.service_commandes.dtos.feign.ClientDTO;
import com.foodcourt.service_commandes.dtos.feign.DishDTO;
import com.foodcourt.service_commandes.dtos.feign.OrderEnrichedResponseDTO;
import com.foodcourt.service_commandes.entities.MealOrder;
import com.foodcourt.service_commandes.entities.OrderItem;
import com.foodcourt.service_commandes.entities.OrderStatus;
import com.foodcourt.service_commandes.exceptions.ResourceNotFoundException;
import com.foodcourt.service_commandes.repositories.MealOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealOrderService {

    private final MealOrderRepository orderRepository;
    private final ClientFeignClient clientFeignClient;
    private final MenuFeignClient menuFeignClient;

    @Transactional
    public MealOrderResponseDTO createOrder(MealOrderRequestDTO dto) {
        // 1. Validation préalable du client via OpenFeign (service-usagers)
        ClientDTO client = clientFeignClient.getClientById(dto.userId());
        if (client != null && Boolean.FALSE.equals(client.getActif())) {
            throw new IllegalArgumentException("Le compte client ID " + dto.userId() + " est désactivé.");
        }

        // 2. Validation préalable des plats via OpenFeign (service-menu)
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
        MealOrder saved = orderRepository.save(order);
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

    // Endpoint V4 : Agrégation & Consultation Enrichie
    public OrderEnrichedResponseDTO getOrderEnrichedById(Long id) {
        MealOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec ID : " + id));

        // Appel synchrone vers service-usagers
        ClientDTO client = clientFeignClient.getClientById(order.getUserId());

        // Appel synchrone vers service-menu pour chaque plat
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
        return mapToDTO(orderRepository.save(order));
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