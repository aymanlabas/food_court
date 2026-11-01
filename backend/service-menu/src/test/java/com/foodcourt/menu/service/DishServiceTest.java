package com.foodcourt.menu.service;

import com.foodcourt.menu.dto.DishRequest;
import com.foodcourt.menu.dto.DishResponse;
import com.foodcourt.menu.dto.DishValidationResponse;
import com.foodcourt.menu.entity.Categorie;
import com.foodcourt.menu.entity.Dish;
import com.foodcourt.menu.exception.ResourceNotFoundException;
import com.foodcourt.menu.mapper.DishMapper;
import com.foodcourt.menu.repository.DishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishServiceTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private DishMapper dishMapper;

    @InjectMocks
    private DishService dishService;

    private Dish dish;
    private DishRequest dishRequest;
    private DishResponse dishResponse;

    @BeforeEach
    void setUp() {
        dish = Dish.builder()
                .id(1L)
                .nom("Salade César")
                .description("Salade fraîche")
                .prix(BigDecimal.valueOf(8.50))
                .categorie(Categorie.ENTREE)
                .disponible(true)
                .build();

        dishRequest = DishRequest.builder()
                .nom("Salade César")
                .description("Salade fraîche")
                .prix(BigDecimal.valueOf(8.50))
                .categorie(Categorie.ENTREE)
                .disponible(true)
                .build();

        dishResponse = DishResponse.builder()
                .id(1L)
                .nom("Salade César")
                .prix(BigDecimal.valueOf(8.50))
                .categorie(Categorie.ENTREE)
                .disponible(true)
                .build();
    }

    @Test
    void shouldReturnAllAvailableDishes() {
        when(dishRepository.findByDisponibleTrue()).thenReturn(List.of(dish));
        when(dishMapper.toResponseList(any())).thenReturn(List.of(dishResponse));

        List<DishResponse> result = dishService.getAvailableDishes();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(dishRepository, times(1)).findByDisponibleTrue();
    }

    @Test
    void shouldReturnDishById() {
        when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
        when(dishMapper.toResponse(dish)).thenReturn(dishResponse);

        DishResponse result = dishService.getDishById(1L);

        assertNotNull(result);
        assertEquals("Salade César", result.getNom());
    }

    @Test
    void shouldThrowWhenDishNotFound() {
        when(dishRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dishService.getDishById(99L));
    }

    @Test
    void shouldCreateDishSuccessfully() {
        when(dishMapper.toEntity(dishRequest)).thenReturn(dish);
        when(dishRepository.save(dish)).thenReturn(dish);
        when(dishMapper.toResponse(dish)).thenReturn(dishResponse);

        DishResponse result = dishService.createDish(dishRequest);

        assertNotNull(result);
        assertEquals("Salade César", result.getNom());
        verify(dishRepository, times(1)).save(dish);
    }

    @Test
    void shouldValidateDishSuccessfully() {
        when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));

        DishValidationResponse validation = dishService.validateDishForOrder(1L);

        assertTrue(validation.isValid());
        assertTrue(validation.isDisponible());
        assertEquals(BigDecimal.valueOf(8.50), validation.getPrix());
    }
}

