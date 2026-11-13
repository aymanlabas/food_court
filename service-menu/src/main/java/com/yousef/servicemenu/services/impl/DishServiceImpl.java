package com.yousef.servicemenu.services.impl;



import com.yousef.servicemenu.dtos.request.DishRequest;
import com.yousef.servicemenu.dtos.response.DishResponse;
import com.yousef.servicemenu.entities.Dish;
import com.yousef.servicemenu.exceptions.ResourceNotFoundException;
import com.yousef.servicemenu.mapper.DishMapper;
import com.yousef.servicemenu.repositories.DishRepository;
import com.yousef.servicemenu.services.DishService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final DishMapper dishMapper;

    @Override
    public DishResponse createDish(DishRequest request) {

        Dish dish = dishMapper.toEntity(request);

        Dish savedDish = dishRepository.save(dish);

        return dishMapper.toResponse(savedDish);
    }

    @Override
    @Transactional(readOnly = true)
    public DishResponse getDishById(Long id) {

        Dish dish = findDishById(id);

        return dishMapper.toResponse(dish);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishResponse> getAllDishes() {

        return dishRepository.findAll()
                .stream()
                .map(dishMapper::toResponse)
                .toList();
    }

    @Override
    public DishResponse updateDish(Long id, DishRequest request) {

        Dish existingDish = findDishById(id);

        dishMapper.updateEntity(request, existingDish);

        Dish updatedDish = dishRepository.save(existingDish);

        return dishMapper.toResponse(updatedDish);
    }

    @Override
    public void deleteDish(Long id) {

        Dish dish = findDishById(id);

        dishRepository.delete(dish);
    }

    private Dish findDishById(Long id) {

        return dishRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aucun plat trouvé avec l'identifiant : " + id
                        )
                );
    }
}

