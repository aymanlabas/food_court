package com.foodcourt.menu.controller;

import com.foodcourt.menu.dto.*;
import com.foodcourt.menu.service.DishService;
import com.foodcourt.menu.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/menu")
@RequiredArgsConstructor
@Tag(name = "Internal Menu", description = "Endpoints internes pour la communication inter-microservices")
public class InternalMenuController {

    private final DishService dishService;
    private final StockService stockService;

    @GetMapping("/dishes/{id}/validate")
    @Operation(summary = "Valider l'existence, le prix et la disponibilité d'un plat")
    public ResponseEntity<DishValidationResponse> validateDish(@PathVariable Long id) {
        return ResponseEntity.ok(dishService.validateDishForOrder(id));
    }

    @PostMapping("/dishes/check-availability")
    @Operation(summary = "Vérifier la disponibilité des ingrédients pour un panier de plats")
    public ResponseEntity<IngredientReservationResponse> checkDishesAvailability(
            @Valid @RequestBody DishAvailabilityRequest request) {
        return ResponseEntity.ok(stockService.checkAvailability(request));
    }

    @PostMapping("/ingredients/reserve")
    @Operation(summary = "Réserver les ingrédients pour une commande")
    public ResponseEntity<IngredientReservationResponse> reserveIngredients(
            @Valid @RequestBody IngredientReservationRequest request) {
        return ResponseEntity.ok(stockService.reserveIngredients(request));
    }

    @PostMapping("/ingredients/release")
    @Operation(summary = "Libérer les ingrédients réservés après annulation")
    public ResponseEntity<Void> releaseIngredients(
            @RequestBody Map<Long, BigDecimal> ingredientQuantities) {
        stockService.releaseIngredients(ingredientQuantities);
        return ResponseEntity.ok().build();
    }
}

