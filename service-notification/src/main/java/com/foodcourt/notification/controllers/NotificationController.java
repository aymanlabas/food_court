package com.foodcourt.notification.controllers;

import com.foodcourt.notification.dtos.NotificationRequest;
import com.foodcourt.notification.dtos.NotificationResponse;
import com.foodcourt.notification.services.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponse> findByUser(@PathVariable Long userId) {
        return service.findByUser(userId);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id) {
        return service.markAsRead(id);
    }
}
