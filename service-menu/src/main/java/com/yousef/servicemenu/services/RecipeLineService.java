package com.yousef.servicemenu.services;



import com.yousef.servicemenu.dtos.request.RecipeLineRequest;
import com.yousef.servicemenu.dtos.response.RecipeLineResponse;

import java.util.List;

public interface RecipeLineService {

    RecipeLineResponse createRecipeLine(RecipeLineRequest request);

    RecipeLineResponse getRecipeLineById(Long id);

    List<RecipeLineResponse> getAllRecipeLines();

    RecipeLineResponse updateRecipeLine(
            Long id,
            RecipeLineRequest request
    );

    void deleteRecipeLine(Long id);
}

