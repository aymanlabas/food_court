package com.foodcourt.service_commandes.clients;

import com.foodcourt.service_commandes.dtos.feign.ClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-usagers", fallbackFactory = ClientFeignClientFallbackFactory.class)
public interface ClientFeignClient {

    @GetMapping("/api/usagers/clients/{id}")
    ClientDTO getClientById(@PathVariable("id") Long id);
}