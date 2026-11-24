package com.yousef.servicemenu.repositories;



import com.yousef.servicemenu.entities.RecipeLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeLineRepository extends JpaRepository<RecipeLine, Long> {

    boolean existsByDish_IdAndIngredient_Id(
            Long dishId,
            Long ingredientId
    );

    boolean existsByDish_IdAndIngredient_IdAndIdNot(
            Long dishId,
            Long ingredientId,
            Long id
    );
}
