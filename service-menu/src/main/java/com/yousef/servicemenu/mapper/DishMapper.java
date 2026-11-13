package com.yousef.servicemenu.mapper;



import org.mapstruct.Mapping;
import com.yousef.servicemenu.dtos.request.DishRequest;
import com.yousef.servicemenu.dtos.response.DishResponse;
import com.yousef.servicemenu.entities.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface DishMapper {

    @Mapping(target = "id", ignore = true)
    Dish toEntity(DishRequest request);

    DishResponse toResponse(Dish dish);

    @Mapping(target = "id", ignore = true)
    void updateEntity(
            DishRequest request,
            @MappingTarget Dish dish
    );
}

