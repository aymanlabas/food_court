package com.foodcourt.menu.service;

import com.foodcourt.menu.dto.DishAvailabilityRequest;
import com.foodcourt.menu.dto.IngredientReservationRequest;
import com.foodcourt.menu.dto.IngredientReservationResponse;
import com.foodcourt.menu.entity.Dish;
import com.foodcourt.menu.entity.Ingredient;
import com.foodcourt.menu.entity.RecipeLine;
import com.foodcourt.menu.exception.InsufficientStockException;
import com.foodcourt.menu.exception.ResourceNotFoundException;
import com.foodcourt.menu.repository.DishRepository;
import com.foodcourt.menu.repository.IngredientRepository;
import com.foodcourt.menu.repository.RecipeLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeLineRepository recipeLineRepository;

    /**
     * Calcule la quantité totale nécessaire par ingrédient pour un panier donné de plats et quantités.
     */
    public Map<Long, BigDecimal> calculateRequirements(Map<Long, Integer> dishQuantities) {
        log.info("Calculating ingredient requirements for dishes: {}", dishQuantities);
        Map<Long, BigDecimal> requiredIngredients = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : dishQuantities.entrySet()) {
            Long dishId = entry.getKey();
            int qty = entry.getValue();

            List<RecipeLine> recipeLines = recipeLineRepository.findByDishId(dishId);
            for (RecipeLine line : recipeLines) {
                Long ingredientId = line.getIngredient().getId();
                BigDecimal neededForDish = line.getQuantite().multiply(BigDecimal.valueOf(qty));
                requiredIngredients.merge(ingredientId, neededForDish, BigDecimal::add);
            }
        }
        return requiredIngredients;
    }

    /**
     * Vérifie la disponibilité des plats en comparant les stocks nécessaires.
     */
    @Transactional(readOnly = true)
    public IngredientReservationResponse checkAvailability(DishAvailabilityRequest request) {
        log.info("Checking availability for dish quantities: {}", request.getDishQuantities());

        Map<Long, BigDecimal> requirements = calculateRequirements(request.getDishQuantities());
        List<String> insufficient = new ArrayList<>();
        Map<Long, String> details = new HashMap<>();

        for (Map.Entry<Long, BigDecimal> entry : requirements.entrySet()) {
            Long ingId = entry.getKey();
            BigDecimal needed = entry.getValue();

            Ingredient ingredient = ingredientRepository.findById(ingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ingrédient introuvable : " + ingId));

            if (ingredient.getStock().compareTo(needed) < 0) {
                String detail = String.format("%s (disponible: %s %s, nécessaire: %s %s)",
                        ingredient.getNom(), ingredient.getStock(), ingredient.getUnite(), needed, ingredient.getUnite());
                insufficient.add(ingredient.getNom());
                details.put(ingId, detail);
            }
        }

        boolean success = insufficient.isEmpty();
        return IngredientReservationResponse.builder()
                .success(success)
                .message(success ? "Tous les ingrédients sont disponibles" : "Stock insuffisant pour certains ingrédients")
                .insufficientIngredients(insufficient)
                .details(details)
                .build();
    }

    /**
     * Réserve les ingrédients atomiquement. Échoue si le stock est insuffisant.
     */
    @Transactional
    public IngredientReservationResponse reserveIngredients(IngredientReservationRequest request) {
        log.info("Reserving ingredients: {}", request.getIngredientQuantities());

        List<String> insufficient = new ArrayList<>();
        Map<Long, String> details = new HashMap<>();

        // Première passe de vérification
        for (Map.Entry<Long, BigDecimal> entry : request.getIngredientQuantities().entrySet()) {
            Long ingId = entry.getKey();
            BigDecimal requestedQty = entry.getValue();

            Ingredient ingredient = ingredientRepository.findById(ingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ingrédient introuvable : " + ingId));

            if (ingredient.getStock().compareTo(requestedQty) < 0) {
                insufficient.add(ingredient.getNom());
                details.put(ingId, "Stock insuffisant pour " + ingredient.getNom());
            }
        }

        if (!insufficient.isEmpty()) {
            log.warn("Cannot reserve ingredients due to insufficient stock: {}", insufficient);
            return IngredientReservationResponse.builder()
                    .success(false)
                    .message("Échec de la réservation : stock insuffisant")
                    .insufficientIngredients(insufficient)
                    .details(details)
                    .build();
        }

        // Seconde passe : décrémentation atomique
        for (Map.Entry<Long, BigDecimal> entry : request.getIngredientQuantities().entrySet()) {
            Long ingId = entry.getKey();
            BigDecimal requestedQty = entry.getValue();

            int updated = ingredientRepository.decrementStock(ingId, requestedQty);
            if (updated == 0) {
                throw new InsufficientStockException("Stock insuffisant pour l'ingrédient ID : " + ingId);
            }
        }

        // Recalcul de la disponibilité automatique des plats
        recalculateDishesAvailability();

        return IngredientReservationResponse.builder()
                .success(true)
                .message("Ingrédients réservés avec succès")
                .insufficientIngredients(Collections.emptyList())
                .details(Collections.emptyMap())
                .build();
    }

    /**
     * Libère les ingrédients réservés suite à une annulation.
     */
    @Transactional
    public void releaseIngredients(Map<Long, BigDecimal> ingredientQuantities) {
        log.info("Releasing ingredients: {}", ingredientQuantities);

        for (Map.Entry<Long, BigDecimal> entry : ingredientQuantities.entrySet()) {
            Long ingId = entry.getKey();
            BigDecimal qty = entry.getValue();
            ingredientRepository.incrementStock(ingId, qty);
        }

        recalculateDishesAvailability();
    }

    /**
     * Recalcule automatiquement la disponibilité de chaque plat selon les stocks actuels des ingrédients.
     */
    @Transactional
    public void recalculateDishesAvailability() {
        log.info("Recalculating dishes availability based on current stock");
        List<Dish> dishes = dishRepository.findAll();

        for (Dish dish : dishes) {
            List<RecipeLine> recipeLines = recipeLineRepository.findByDishId(dish.getId());
            if (recipeLines.isEmpty()) {
                continue;
            }

            boolean canFulfill = recipeLines.stream().allMatch(line -> {
                Ingredient ing = ingredientRepository.findById(line.getIngredient().getId()).orElse(null);
                return ing != null && ing.getStock().compareTo(line.getQuantite()) >= 0;
            });

            if (dish.isDisponible() != canFulfill) {
                log.info("Updating dish {} availability to {}", dish.getNom(), canFulfill);
                dish.setDisponible(canFulfill);
                dishRepository.save(dish);
            }
        }
    }
}

