package com.yousef.servicemenu.services.impl;



import com.yousef.servicemenu.dtos.request.RecipeLineRequest;
import com.yousef.servicemenu.dtos.response.RecipeLineResponse;
import com.yousef.servicemenu.entities.Dish;
import com.yousef.servicemenu.entities.Ingredient;
import com.yousef.servicemenu.entities.RecipeLine;
import com.yousef.servicemenu.exceptions.ResourceNotFoundException;
import com.yousef.servicemenu.mapper.RecipeLineMapper;
import com.yousef.servicemenu.repositories.DishRepository;
import com.yousef.servicemenu.repositories.IngredientRepository;
import com.yousef.servicemenu.repositories.RecipeLineRepository;
import com.yousef.servicemenu.services.RecipeLineService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipeLineServiceImpl implements RecipeLineService {

    private final RecipeLineRepository recipeLineRepository;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeLineMapper recipeLineMapper;

    @Override
    public RecipeLineResponse createRecipeLine(
            RecipeLineRequest request) {

        Dish dish = findDishById(request.getDishId());

        com.yousef.servicemenu.entities.Ingredient ingredient =
                findIngredientById(request.getIngredientId());

        validateUniqueIngredient(
                request.getDishId(),
                request.getIngredientId()
        );

        RecipeLine recipeLine = recipeLineMapper.toEntity(request);

        recipeLine.setDish(dish);
        recipeLine.setIngredient(ingredient);

        RecipeLine savedRecipeLine =
                recipeLineRepository.save(recipeLine);

        return recipeLineMapper.toResponse(savedRecipeLine);
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeLineResponse getRecipeLineById(Long id) {

        RecipeLine recipeLine = findRecipeLineById(id);

        return recipeLineMapper.toResponse(recipeLine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeLineResponse> getAllRecipeLines() {

        return recipeLineRepository.findAll()
                .stream()
                .map(recipeLineMapper::toResponse)
                .toList();
    }

    @Override
    public RecipeLineResponse updateRecipeLine(
            Long id,
            RecipeLineRequest request) {

        RecipeLine existingRecipeLine = findRecipeLineById(id);

        Dish dish = findDishById(request.getDishId());

        com.yousef.servicemenu.entities.Ingredient ingredient =
                findIngredientById(request.getIngredientId());

        validateUniqueIngredientForUpdate(
                request.getDishId(),
                request.getIngredientId(),
                id
        );

        recipeLineMapper.updateEntity(request, existingRecipeLine);

        existingRecipeLine.setDish(dish);
        existingRecipeLine.setIngredient(ingredient);

        RecipeLine updatedRecipeLine =
                recipeLineRepository.save(existingRecipeLine);

        return recipeLineMapper.toResponse(updatedRecipeLine);
    }

    @Override
    public void deleteRecipeLine(Long id) {

        RecipeLine recipeLine = findRecipeLineById(id);

        recipeLineRepository.delete(recipeLine);
    }

    private Dish findDishById(Long id) {

        return dishRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aucun plat trouvé avec l'identifiant : " + id
                        )
                );
    }

    private Ingredient findIngredientById(Long id) {

        return ingredientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aucun ingrédient trouvé avec l'identifiant : " + id
                        )
                );
    }

    private RecipeLine findRecipeLineById(Long id) {

        return recipeLineRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aucune ligne de recette trouvée avec l'identifiant : " + id
                        )
                );
    }

    private void validateUniqueIngredient(
            Long dishId,
            Long ingredientId) {

        if (recipeLineRepository.existsByDish_IdAndIngredient_Id(
                dishId, ingredientId)) {

            throw new IllegalArgumentException(
                    "Cet ingrédient existe déjà dans la recette de ce plat"
            );
        }
    }

    private void validateUniqueIngredientForUpdate(
            Long dishId,
            Long ingredientId,
            Long recipeLineId) {

        if (recipeLineRepository.existsByDish_IdAndIngredient_IdAndIdNot(
                dishId, ingredientId, recipeLineId)) {

            throw new IllegalArgumentException(
                    "Cet ingrédient existe déjà dans la recette de ce plat"
            );
        }
    }
}

