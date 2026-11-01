package com.foodcourt.menu.repository;

import com.foodcourt.menu.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByNom(String nom);

    boolean existsByNom(String nom);

    @Query("SELECT i FROM Ingredient i WHERE i.stock <= i.seuilAlerte")
    List<Ingredient> findLowStock();

    @Modifying
    @Query("UPDATE Ingredient i SET i.stock = i.stock - :quantity WHERE i.id = :id AND i.stock >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") BigDecimal quantity);

    @Modifying
    @Query("UPDATE Ingredient i SET i.stock = i.stock + :quantity WHERE i.id = :id")
    int incrementStock(@Param("id") Long id, @Param("quantity") BigDecimal quantity);
}
