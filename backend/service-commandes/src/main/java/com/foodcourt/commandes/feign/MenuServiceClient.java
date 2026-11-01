package com.foodcourt.commandes.feign;

import com.foodcourt.commandes.feign.dto.DishAvailabilityRequest;
import com.foodcourt.commandes.feign.dto.DishValidationResponse;
import com.foodcourt.commandes.feign.dto.IngredientReservationRequest;
import com.foodcourt.commandes.feign.dto.IngredientReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "service-menu", url = "${services.service-menu.url:http://localhost:8081}")
public interface MenuServiceClient {

    @GetMapping("/api/internal/menu/dishes/{id}/validate")
    DishValidationResponse validateDish(@PathVariable("id") Long id);

    @PostMapping("/api/internal/menu/dishes/check-availability")
    IngredientReservationResponse checkDishesAvailability(@RequestBody DishAvailabilityRequest request);

    @PostMapping("/api/internal/menu/ingredients/reserve")
    IngredientReservationResponse reserveIngredients(@RequestBody IngredientReservationRequest request);

    @PostMapping("/api/internal/menu/ingredients/release")
    void releaseIngredients(@RequestBody Map<Long, BigDecimal> ingredientQuantities);
}

