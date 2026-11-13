package com.yousef.servicemenu.services;



import com.yousef.servicemenu.dtos.request.IngredientRequest;
import com.yousef.servicemenu.dtos.response.IngredientResponse;

import java.util.List;

public interface IngredientService {

    IngredientResponse createIngredient(IngredientRequest request);

    IngredientResponse getIngredientById(Long id);

    List<IngredientResponse> getAllIngredients();

    IngredientResponse updateIngredient(
            Long id,
            IngredientRequest request
    );

    void deleteIngredient(Long id);
}

