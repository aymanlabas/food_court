package com.yousef.servicemenu.mapper;



import org.mapstruct.Mapping;
import com.yousef.servicemenu.dtos.request.IngredientRequest;
import com.yousef.servicemenu.dtos.response.IngredientResponse;
import com.yousef.servicemenu.entities.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface IngredientMapper {

    @Mapping(target = "id", ignore = true)
    Ingredient toEntity(IngredientRequest request);

    IngredientResponse toResponse(Ingredient ingredient);

    @Mapping(target = "id", ignore = true)
    void updateEntity(
            IngredientRequest request,
            @MappingTarget Ingredient ingredient
    );
}

