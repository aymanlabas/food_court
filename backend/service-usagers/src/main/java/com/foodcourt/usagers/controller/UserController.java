package com.foodcourt.usagers.controller;

import com.foodcourt.usagers.dto.BalanceUpdateRequest;
import com.foodcourt.usagers.dto.UserResponse;
import com.foodcourt.usagers.dto.UserUpdateRequest;
import com.foodcourt.usagers.entity.User;
import com.foodcourt.usagers.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints de gestion des utilisateurs et soldes")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister tous les utilisateurs (ADMIN)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    @Operation(summary = "Obtenir les informations du profil connecté")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getUserById(currentUser.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Obtenir un utilisateur par son identifiant")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Mettre à jour un utilisateur")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un utilisateur (ADMIN)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Consulter le solde badge cantine")
    public ResponseEntity<BigDecimal> getUserBalance(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserBalance(id));
    }

    @PatchMapping("/{id}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créditer le solde cantine (ADMIN)")
    public ResponseEntity<UserResponse> updateBalance(
            @PathVariable Long id,
            @Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(userService.updateBalance(id, request));
    }
}

