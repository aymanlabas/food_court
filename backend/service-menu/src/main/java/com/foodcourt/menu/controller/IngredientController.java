package com.foodcourt.menu.controller;

import com.foodcourt.menu.dto.IngredientRequest;
import com.foodcourt.menu.dto.IngredientResponse;
import com.foodcourt.menu.dto.StockUpdateRequest;
import com.foodcourt.menu.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Tag(name = "Ingredients", description = "Endpoints de gestion des ingrédients et du stock")
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    @Operation(summary = "Lister tous les ingrédients")
    public ResponseEntity<List<IngredientResponse>> getAllIngredients() {
        return ResponseEntity.ok(ingredientService.getAllIngredients());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un ingrédient par son identifiant")
    public ResponseEntity<IngredientResponse> getIngredientById(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.getIngredientById(id));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Lister les ingrédients en alerte de stock bas")
    public ResponseEntity<List<IngredientResponse>> getLowStockIngredients() {
        return ResponseEntity.ok(ingredientService.getLowStockIngredients());
    }

    @PostMapping
    @Operation(summary = "Créer un nouvel ingrédient")
    public ResponseEntity<IngredientResponse> createIngredient(@Valid @RequestBody IngredientRequest request) {
        IngredientResponse response = ingredientService.createIngredient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un ingrédient")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody IngredientRequest request) {
        return ResponseEntity.ok(ingredientService.updateIngredient(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un ingrédient")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Mettre à jour la quantité en stock d'un ingrédient")
    public ResponseEntity<IngredientResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(ingredientService.updateStock(id, request));
    }
}

