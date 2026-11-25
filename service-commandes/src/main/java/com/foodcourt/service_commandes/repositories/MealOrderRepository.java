package com.foodcourt.service_commandes.repositories;

import com.foodcourt.service_commandes.entities.MealOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealOrderRepository extends JpaRepository<MealOrder, Long> {
    List<MealOrder> findByUserId(Long userId);
    Optional<MealOrder> findByIdempotencyKey(String idempotencyKey);
}