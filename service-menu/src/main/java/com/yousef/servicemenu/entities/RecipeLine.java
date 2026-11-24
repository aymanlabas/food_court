package com.yousef.servicemenu.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "recipe_lines",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recipe_dish_ingredient",
                        columnNames = {"dish_id", "ingredient_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class RecipeLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private com.yousef.servicemenu.entities.Ingredient ingredient;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantite;
}
