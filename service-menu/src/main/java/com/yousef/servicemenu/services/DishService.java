package com.yousef.servicemenu.services;



import com.yousef.servicemenu.dtos.request.DishRequest;
import com.yousef.servicemenu.dtos.response.DishResponse;

import java.util.List;

public interface DishService {

    DishResponse createDish(DishRequest request);

    DishResponse getDishById(Long id);

    List<DishResponse> getAllDishes();

    DishResponse updateDish(Long id, DishRequest request);

    void deleteDish(Long id);
}
