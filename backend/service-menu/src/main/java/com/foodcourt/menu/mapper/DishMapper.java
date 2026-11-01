package com.foodcourt.menu.mapper;

import com.foodcourt.menu.dto.DishRequest;
import com.foodcourt.menu.dto.DishResponse;
import com.foodcourt.menu.entity.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DishMapper {

    Dish toEntity(DishRequest request);

    DishResponse toResponse(Dish dish);

    List<DishResponse> toResponseList(List<Dish> dishes);

    void updateEntityFromRequest(DishRequest request, @MappingTarget Dish dish);
}

