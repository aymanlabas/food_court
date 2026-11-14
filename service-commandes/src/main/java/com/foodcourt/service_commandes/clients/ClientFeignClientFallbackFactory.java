package com.foodcourt.service_commandes.clients;

import com.foodcourt.service_commandes.dtos.feign.ClientDTO;
import com.foodcourt.service_commandes.exceptions.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ClientFeignClientFallbackFactory implements FallbackFactory<ClientFeignClient> {

    @Override
    public ClientFeignClient create(Throwable cause) {
        return id -> {
            if (cause instanceof FeignException.NotFound) {
                throw new ResourceNotFoundException("Client introuvable dans service-usagers avec ID : " + id);
            }
            return ClientDTO.builder()
                    .id(id)
                    .nom("Client #" + id + " (Service Usagers Indisponible)")
                    .email("non-disponible@foodcourt.ma")
                    .telephone("N/A")
                    .adresseLivraison("N/A")
                    .actif(true)
                    .build();
        };
    }
}