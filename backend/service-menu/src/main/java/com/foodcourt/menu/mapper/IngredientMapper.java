package com.foodcourt.menu.mapper;

import com.foodcourt.menu.dto.IngredientRequest;
import com.foodcourt.menu.dto.IngredientResponse;
import com.foodcourt.menu.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    Ingredient toEntity(IngredientRequest request);

    IngredientResponse toResponse(Ingredient ingredient);

    List<IngredientResponse> toResponseList(List<Ingredient> ingredients);

    void updateEntityFromRequest(IngredientRequest request, @MappingTarget Ingredient ingredient);
}

