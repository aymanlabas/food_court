package com.foodcourt.usagers.mapper;

import com.foodcourt.usagers.dto.RegisterRequest;
import com.foodcourt.usagers.dto.UserResponse;
import com.foodcourt.usagers.dto.UserUpdateRequest;
import com.foodcourt.usagers.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "soldeCantine", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);
}

