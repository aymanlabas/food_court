package com.foodcourt.service_commandes.services;

import com.foodcourt.service_commandes.dtos.MealOrderRequestDTO;
import com.foodcourt.service_commandes.dtos.MealOrderResponseDTO;
import com.foodcourt.service_commandes.dtos.OrderItemDTO;
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

    @Transactional
    public MealOrderResponseDTO createOrder(MealOrderRequestDTO dto) {
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