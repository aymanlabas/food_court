package com.yousef.servicemenu.repositories;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.yousef.servicemenu.entities.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    //boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(@NotBlank(message = "Le nom de l'ingrédient est obligatoire") @Size(max = 100) String nom, Long id);

    boolean existsByNomIgnoreCase(@NotBlank(message = "Le nom de l'ingrédient est obligatoire") @Size(max = 100) String nom);
}

