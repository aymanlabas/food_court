package com.foodcourt.service_commandes.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MealOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private LocalDateTime date;
    private Double montantTotal;

    @Enumerated(EnumType.STRING)
    private OrderStatus statut;

    @OneToMany(mappedBy = "mealOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}