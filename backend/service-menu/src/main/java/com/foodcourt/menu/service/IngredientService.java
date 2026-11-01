package com.foodcourt.menu.service;

import com.foodcourt.menu.dto.IngredientRequest;
import com.foodcourt.menu.dto.IngredientResponse;
import com.foodcourt.menu.dto.StockUpdateRequest;
import com.foodcourt.menu.entity.Ingredient;
import com.foodcourt.menu.exception.ResourceNotFoundException;
import com.foodcourt.menu.mapper.IngredientMapper;
import com.foodcourt.menu.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public List<IngredientResponse> getAllIngredients() {
        log.info("Fetching all ingredients");
        return ingredientMapper.toResponseList(ingredientRepository.findAll());
    }

    public IngredientResponse getIngredientById(Long id) {
        log.info("Fetching ingredient with ID: {}", id);
        Ingredient ingredient = findIngredientOrThrow(id);
        return ingredientMapper.toResponse(ingredient);
    }

    public List<IngredientResponse> getLowStockIngredients() {
        log.info("Fetching low stock ingredients");
        return ingredientMapper.toResponseList(ingredientRepository.findLowStock());
    }

    @Transactional
    public IngredientResponse createIngredient(IngredientRequest request) {
        log.info("Creating new ingredient: {}", request.getNom());
        if (ingredientRepository.existsByNom(request.getNom())) {
            throw new IllegalArgumentException("Un ingrédient avec ce nom existe déjà : " + request.getNom());
        }
        Ingredient ingredient = ingredientMapper.toEntity(request);
        Ingredient saved = ingredientRepository.save(ingredient);
        return ingredientMapper.toResponse(saved);
    }

    @Transactional
    public IngredientResponse updateIngredient(Long id, IngredientRequest request) {
        log.info("Updating ingredient with ID: {}", id);
        Ingredient ingredient = findIngredientOrThrow(id);
        if (!ingredient.getNom().equalsIgnoreCase(request.getNom()) &&
                ingredientRepository.existsByNom(request.getNom())) {
            throw new IllegalArgumentException("Un ingrédient avec ce nom existe déjà : " + request.getNom());
        }
        ingredientMapper.updateEntityFromRequest(request, ingredient);
        Ingredient updated = ingredientRepository.save(ingredient);
        return ingredientMapper.toResponse(updated);
    }

    @Transactional
    public void deleteIngredient(Long id) {
        log.info("Deleting ingredient with ID: {}", id);
        Ingredient ingredient = findIngredientOrThrow(id);
        ingredientRepository.delete(ingredient);
    }

    @Transactional
    public IngredientResponse updateStock(Long id, StockUpdateRequest request) {
        log.info("Updating stock for ingredient ID: {} to {}", id, request.getQuantite());
        Ingredient ingredient = findIngredientOrThrow(id);
        ingredient.setStock(request.getQuantite());
        Ingredient saved = ingredientRepository.save(ingredient);
        return ingredientMapper.toResponse(saved);
    }

    private Ingredient findIngredientOrThrow(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrédient non trouvé avec l'identifiant : " + id));
    }
}

