package com.yousef.servicemenu.controllers;



import com.yousef.servicemenu.dtos.request.IngredientRequest;
import com.yousef.servicemenu.dtos.response.IngredientResponse;
import com.yousef.servicemenu.services.IngredientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    public ResponseEntity<IngredientResponse> createIngredient(
            @Valid @RequestBody IngredientRequest request) {

        IngredientResponse response =
                ingredientService.createIngredient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> getIngredientById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ingredientService.getIngredientById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getAllIngredients() {

        return ResponseEntity.ok(
                ingredientService.getAllIngredients()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody IngredientRequest request) {

        return ResponseEntity.ok(
                ingredientService.updateIngredient(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(
            @PathVariable Long id) {

        ingredientService.deleteIngredient(id);

        return ResponseEntity.noContent().build();
    }
}

