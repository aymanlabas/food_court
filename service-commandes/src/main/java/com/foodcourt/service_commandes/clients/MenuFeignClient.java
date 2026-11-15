package com.foodcourt.service_commandes.clients;

import com.foodcourt.service_commandes.dtos.feign.DishDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-menu", fallback = MenuFeignClientFallback.class)
public interface MenuFeignClient {

    @GetMapping("/api/dishes/{id}")
    DishDTO getDishById(@PathVariable("id") Long id);
}