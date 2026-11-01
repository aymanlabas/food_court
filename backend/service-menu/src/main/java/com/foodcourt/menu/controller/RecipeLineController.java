package com.foodcourt.menu.controller;

import com.foodcourt.menu.dto.RecipeLineRequest;
import com.foodcourt.menu.dto.RecipeLineResponse;
import com.foodcourt.menu.service.RecipeLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe-lines")
@RequiredArgsConstructor
@Tag(name = "Recipe Lines", description = "Endpoints de gestion des recettes (association plat - ingrédient)")
public class RecipeLineController {

    private final RecipeLineService recipeLineService;

    @GetMapping
    @Operation(summary = "Lister toutes les lignes de recettes")
    public ResponseEntity<List<RecipeLineResponse>> getAllRecipeLines() {
        return ResponseEntity.ok(recipeLineService.getAllRecipeLines());
    }

    @GetMapping("/dish/{dishId}")
    @Operation(summary = "Lister les lignes de recette pour un plat spécifique")
    public ResponseEntity<List<RecipeLineResponse>> getRecipeLinesByDishId(@PathVariable Long dishId) {
        return ResponseEntity.ok(recipeLineService.getRecipeLinesByDishId(dishId));
    }

    @PostMapping
    @Operation(summary = "Créer une ligne de recette")
    public ResponseEntity<RecipeLineResponse> createRecipeLine(@Valid @RequestBody RecipeLineRequest request) {
        RecipeLineResponse response = recipeLineService.createRecipeLine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une ligne de recette")
    public ResponseEntity<RecipeLineResponse> updateRecipeLine(
            @PathVariable Long id,
            @Valid @RequestBody RecipeLineRequest request) {
        return ResponseEntity.ok(recipeLineService.updateRecipeLine(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une ligne de recette")
    public ResponseEntity<Void> deleteRecipeLine(@PathVariable Long id) {
        recipeLineService.deleteRecipeLine(id);
        return ResponseEntity.noContent().build();
    }
}

