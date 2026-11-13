
package com.yousef.servicemenu.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false, length = 50)
    private String categorie;

    @ElementCollection
    @CollectionTable(
            name = "dish_allergens",
            joinColumns = @JoinColumn(name = "dish_id")
    )
    @Column(name = "allergen", nullable = false)
    private Set<String> allergenes = new HashSet<>();

    @Column(nullable = false)
    private boolean disponible;
}

