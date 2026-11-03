package com.foodcourt.serviceusagers.controllers;

import com.foodcourt.serviceusagers.dtos.RestaurantRequest;
import com.foodcourt.serviceusagers.dtos.RestaurantResponse;
import com.foodcourt.serviceusagers.services.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usagers/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public List<RestaurantResponse> lister(@RequestParam(required = false) Boolean actif) {
        return restaurantService.lister(actif);
    }

    @GetMapping("/{id}")
    public RestaurantResponse trouver(@PathVariable Long id) {
        return restaurantService.trouver(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse creer(@Valid @RequestBody RestaurantRequest request) {
        return restaurantService.creer(request);
    }

    @PutMapping("/{id}")
    public RestaurantResponse modifier(@PathVariable Long id, @Valid @RequestBody RestaurantRequest request) {
        return restaurantService.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactiver(@PathVariable Long id) {
        restaurantService.desactiver(id);
    }
}
