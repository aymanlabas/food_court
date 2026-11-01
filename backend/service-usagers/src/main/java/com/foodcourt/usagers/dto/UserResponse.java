package com.foodcourt.usagers.dto;

import com.foodcourt.usagers.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String nom;
    private String email;
    private BigDecimal soldeCantine;
    private String regimeAlimentaire;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
