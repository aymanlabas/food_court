package com.foodcourt.menu.controller;

import com.foodcourt.menu.dto.DishRequest;
import com.foodcourt.menu.dto.DishResponse;
import com.foodcourt.menu.entity.Categorie;
import com.foodcourt.menu.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
@Tag(name = "Dishes", description = "Endpoints de gestion des plats du menu")
public class DishController {

    private final DishService dishService;

    @GetMapping
    @Operation(summary = "Lister tous les plats avec filtres optionnels")
    public ResponseEntity<List<DishResponse>> getAllDishes(
            @RequestParam(required = false) Categorie categorie,
            @RequestParam(required = false) Boolean disponible,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(dishService.getAllDishes(categorie, disponible, search));
    }

    @GetMapping("/available")
    @Operation(summary = "Lister les plats disponibles (menu du jour)")
    public ResponseEntity<List<DishResponse>> getAvailableDishes() {
        return ResponseEntity.ok(dishService.getAvailableDishes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un plat par son identifiant")
    public ResponseEntity<DishResponse> getDishById(@PathVariable Long id) {
        return ResponseEntity.ok(dishService.getDishById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau plat")
    public ResponseEntity<DishResponse> createDish(@Valid @RequestBody DishRequest request) {
        DishResponse response = dishService.createDish(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un plat existant")
    public ResponseEntity<DishResponse> updateDish(
            @PathVariable Long id,
            @Valid @RequestBody DishRequest request) {
        return ResponseEntity.ok(dishService.updateDish(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un plat")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Basculer la disponibilité d'un plat")
    public ResponseEntity<DishResponse> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(dishService.toggleAvailability(id));
    }
}

