package com.foodcourt.service_commandes.controllers;

import com.foodcourt.service_commandes.dtos.MealOrderRequestDTO;
import com.foodcourt.service_commandes.dtos.MealOrderResponseDTO;
import com.foodcourt.service_commandes.entities.OrderStatus;
import com.foodcourt.service_commandes.services.MealOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class MealOrderController {

    private final MealOrderService orderService;

    @PostMapping
    public ResponseEntity<MealOrderResponseDTO> createOrder(@Valid @RequestBody MealOrderRequestDTO dto) {
        return new ResponseEntity<>(orderService.createOrder(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MealOrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealOrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MealOrderResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}