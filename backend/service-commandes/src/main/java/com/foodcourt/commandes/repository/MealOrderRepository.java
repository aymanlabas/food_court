package com.foodcourt.commandes.repository;

import com.foodcourt.commandes.entity.MealOrder;
import com.foodcourt.commandes.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealOrderRepository extends JpaRepository<MealOrder, Long> {

    List<MealOrder> findByUserIdOrderByDateDesc(Long userId);

    Page<MealOrder> findByUserId(Long userId, Pageable pageable);

    Optional<MealOrder> findByIdempotencyKey(UUID idempotencyKey);

    boolean existsByIdempotencyKey(UUID idempotencyKey);

    List<MealOrder> findByStatutInOrderByDateAsc(List<OrderStatus> statuts);

    List<MealOrder> findByStatutOrderByDateAsc(OrderStatus statut);

    long countByStatut(OrderStatus statut);
}

