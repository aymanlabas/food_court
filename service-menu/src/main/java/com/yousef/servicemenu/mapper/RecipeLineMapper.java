package com.yousef.servicemenu.mapper;



import com.yousef.servicemenu.dtos.request.RecipeLineRequest;
import com.yousef.servicemenu.dtos.response.RecipeLineResponse;
import com.yousef.servicemenu.entities.RecipeLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface RecipeLineMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    RecipeLine toEntity(RecipeLineRequest request);

    @Mapping(target = "dishId", source = "dish.id")
    @Mapping(target = "dishNom", source = "dish.nom")
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientNom", source = "ingredient.nom")
    RecipeLineResponse toResponse(RecipeLine recipeLine);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    void updateEntity(
            RecipeLineRequest request,
            @MappingTarget RecipeLine recipeLine
    );
}

