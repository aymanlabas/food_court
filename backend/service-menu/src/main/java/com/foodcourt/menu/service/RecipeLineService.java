package com.foodcourt.menu.service;

import com.foodcourt.menu.dto.RecipeLineRequest;
import com.foodcourt.menu.dto.RecipeLineResponse;
import com.foodcourt.menu.entity.Dish;
import com.foodcourt.menu.entity.Ingredient;
import com.foodcourt.menu.entity.RecipeLine;
import com.foodcourt.menu.exception.ResourceNotFoundException;
import com.foodcourt.menu.mapper.RecipeLineMapper;
import com.foodcourt.menu.repository.DishRepository;
import com.foodcourt.menu.repository.IngredientRepository;
import com.foodcourt.menu.repository.RecipeLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecipeLineService {

    private final RecipeLineRepository recipeLineRepository;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeLineMapper recipeLineMapper;

    public List<RecipeLineResponse> getAllRecipeLines() {
        log.info("Fetching all recipe lines");
        return recipeLineMapper.toResponseList(recipeLineRepository.findAll());
    }

    public List<RecipeLineResponse> getRecipeLinesByDishId(Long dishId) {
        log.info("Fetching recipe lines for dish ID: {}", dishId);
        return recipeLineMapper.toResponseList(recipeLineRepository.findByDishId(dishId));
    }

    @Transactional
    public RecipeLineResponse createRecipeLine(RecipeLineRequest request) {
        log.info("Creating recipe line for dish {} and ingredient {}", request.getDishId(), request.getIngredientId());

        if (recipeLineRepository.existsByDishIdAndIngredientId(request.getDishId(), request.getIngredientId())) {
            throw new IllegalArgumentException("Cet ingrédient est déjà associé à ce plat dans sa recette");
        }

        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'id : " + request.getDishId()));

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new ResourceNotFoundException("Ingrédient non trouvé avec l'id : " + request.getIngredientId()));

        RecipeLine recipeLine = RecipeLine.builder()
                .dish(dish)
                .ingredient(ingredient)
                .quantite(request.getQuantite())
                .build();

        RecipeLine saved = recipeLineRepository.save(recipeLine);
        return recipeLineMapper.toResponse(saved);
    }

    @Transactional
    public RecipeLineResponse updateRecipeLine(Long id, RecipeLineRequest request) {
        log.info("Updating recipe line ID: {}", id);
        RecipeLine recipeLine = recipeLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne de recette non trouvée avec l'id : " + id));

        if (!recipeLine.getDish().getId().equals(request.getDishId())) {
            Dish dish = dishRepository.findById(request.getDishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'id : " + request.getDishId()));
            recipeLine.setDish(dish);
        }

        if (!recipeLine.getIngredient().getId().equals(request.getIngredientId())) {
            Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingrédient non trouvé avec l'id : " + request.getIngredientId()));
            recipeLine.setIngredient(ingredient);
        }

        recipeLine.setQuantite(request.getQuantite());
        RecipeLine updated = recipeLineRepository.save(recipeLine);
        return recipeLineMapper.toResponse(updated);
    }

    @Transactional
    public void deleteRecipeLine(Long id) {
        log.info("Deleting recipe line ID: {}", id);
        RecipeLine recipeLine = recipeLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne de recette non trouvée avec l'id : " + id));
        recipeLineRepository.delete(recipeLine);
    }
}

