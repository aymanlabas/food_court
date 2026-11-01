package com.foodcourt.menu.service;

import com.foodcourt.menu.dto.DishRequest;
import com.foodcourt.menu.dto.DishResponse;
import com.foodcourt.menu.dto.DishValidationResponse;
import com.foodcourt.menu.entity.Categorie;
import com.foodcourt.menu.entity.Dish;
import com.foodcourt.menu.exception.ResourceNotFoundException;
import com.foodcourt.menu.mapper.DishMapper;
import com.foodcourt.menu.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DishService {

    private final DishRepository dishRepository;
    private final DishMapper dishMapper;

    public List<DishResponse> getAllDishes(Categorie categorie, Boolean disponible, String search) {
        log.info("Fetching dishes with filters - categorie: {}, disponible: {}, search: {}", categorie, disponible, search);
        List<Dish> dishes = dishRepository.findWithFilters(categorie, disponible, search);
        return dishMapper.toResponseList(dishes);
    }

    public List<DishResponse> getAvailableDishes() {
        log.info("Fetching all available dishes");
        return dishMapper.toResponseList(dishRepository.findByDisponibleTrue());
    }

    public DishResponse getDishById(Long id) {
        log.info("Fetching dish with ID: {}", id);
        Dish dish = findDishOrThrow(id);
        return dishMapper.toResponse(dish);
    }

    @Transactional
    public DishResponse createDish(DishRequest request) {
        log.info("Creating new dish: {}", request.getNom());
        Dish dish = dishMapper.toEntity(request);
        if (request.getDisponible() != null) {
            dish.setDisponible(request.getDisponible());
        } else {
            dish.setDisponible(true);
        }
        Dish savedDish = dishRepository.save(dish);
        return dishMapper.toResponse(savedDish);
    }

    @Transactional
    public DishResponse updateDish(Long id, DishRequest request) {
        log.info("Updating dish with ID: {}", id);
        Dish dish = findDishOrThrow(id);
        dishMapper.updateEntityFromRequest(request, dish);
        if (request.getDisponible() != null) {
            dish.setDisponible(request.getDisponible());
        }
        Dish updatedDish = dishRepository.save(dish);
        return dishMapper.toResponse(updatedDish);
    }

    @Transactional
    public void deleteDish(Long id) {
        log.info("Deleting dish with ID: {}", id);
        Dish dish = findDishOrThrow(id);
        dishRepository.delete(dish);
    }

    @Transactional
    public DishResponse toggleAvailability(Long id) {
        log.info("Toggling availability for dish ID: {}", id);
        Dish dish = findDishOrThrow(id);
        dish.setDisponible(!dish.isDisponible());
        Dish updated = dishRepository.save(dish);
        return dishMapper.toResponse(updated);
    }

    public DishValidationResponse validateDishForOrder(Long id) {
        log.info("Validating dish for order, ID: {}", id);
        return dishRepository.findById(id)
                .map(dish -> DishValidationResponse.builder()
                        .dishId(dish.getId())
                        .nom(dish.getNom())
                        .prix(dish.getPrix())
                        .disponible(dish.isDisponible())
                        .valid(dish.isDisponible())
                        .message(dish.isDisponible() ? "Plat disponible" : "Plat actuellement indisponible")
                        .build())
                .orElse(DishValidationResponse.builder()
                        .dishId(id)
                        .valid(false)
                        .disponible(false)
                        .message("Plat introuvable")
                        .build());
    }

    private Dish findDishOrThrow(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plat non trouvé avec l'identifiant : " + id));
    }
}

