package com.yousef.servicemenu.controllers;



import com.yousef.servicemenu.dtos.request.RecipeLineRequest;
import com.yousef.servicemenu.dtos.response.RecipeLineResponse;
import com.yousef.servicemenu.services.RecipeLineService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipe-lines")
@RequiredArgsConstructor
public class RecipeLineController {

    private final RecipeLineService recipeLineService;

    @PostMapping
    public ResponseEntity<RecipeLineResponse> createRecipeLine(
            @Valid @RequestBody RecipeLineRequest request) {

        RecipeLineResponse response =
                recipeLineService.createRecipeLine(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeLineResponse> getRecipeLineById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                recipeLineService.getRecipeLineById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<RecipeLineResponse>> getAllRecipeLines() {

        return ResponseEntity.ok(
                recipeLineService.getAllRecipeLines()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeLineResponse> updateRecipeLine(
            @PathVariable Long id,
            @Valid @RequestBody RecipeLineRequest request) {

        return ResponseEntity.ok(
                recipeLineService.updateRecipeLine(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipeLine(
            @PathVariable Long id) {

        recipeLineService.deleteRecipeLine(id);

        return ResponseEntity.noContent().build();
    }
}

