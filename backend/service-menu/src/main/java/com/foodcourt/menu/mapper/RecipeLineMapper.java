package com.foodcourt.menu.mapper;

import com.foodcourt.menu.dto.RecipeLineResponse;
import com.foodcourt.menu.entity.RecipeLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipeLineMapper {

    @Mapping(target = "dishId", source = "dish.id")
    @Mapping(target = "dishNom", source = "dish.nom")
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientNom", source = "ingredient.nom")
    RecipeLineResponse toResponse(RecipeLine recipeLine);

    List<RecipeLineResponse> toResponseList(List<RecipeLine> recipeLines);
}

