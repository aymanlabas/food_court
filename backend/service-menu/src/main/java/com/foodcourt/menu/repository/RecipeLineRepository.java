package com.foodcourt.menu.repository;

import com.foodcourt.menu.entity.RecipeLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeLineRepository extends JpaRepository<RecipeLine, Long> {

    List<RecipeLine> findByDishId(Long dishId);

    List<RecipeLine> findByIngredientId(Long ingredientId);

    boolean existsByDishIdAndIngredientId(Long dishId, Long ingredientId);

    void deleteByDishId(Long dishId);
}
