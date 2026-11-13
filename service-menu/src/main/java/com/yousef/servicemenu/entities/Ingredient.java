package com.yousef.servicemenu.entities;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal stock;

    @Column(nullable = false, length = 20)
    private String unite;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal seuilAlerte;
}

