package com.yousef.servicemenu.controllers;



import com.yousef.servicemenu.dtos.request.DishRequest;
import com.yousef.servicemenu.dtos.response.DishResponse;
import com.yousef.servicemenu.services.DishService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PostMapping
    public ResponseEntity<DishResponse> createDish(
            @Valid @RequestBody DishRequest request) {

        DishResponse response = dishService.createDish(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishResponse> getDishById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                dishService.getDishById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<DishResponse>> getAllDishes() {

        return ResponseEntity.ok(
                dishService.getAllDishes()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishResponse> updateDish(
            @PathVariable Long id,
            @Valid @RequestBody DishRequest request) {

        return ResponseEntity.ok(
                dishService.updateDish(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(
            @PathVariable Long id) {

        dishService.deleteDish(id);

        return ResponseEntity.noContent().build();
    }
}
