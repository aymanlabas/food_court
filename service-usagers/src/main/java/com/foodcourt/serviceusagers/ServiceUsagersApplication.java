package com.foodcourt.serviceusagers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Point d'entrée du service-usagers.
 *
 * @EnableFeignClients : active OpenFeign pour les appels synchrones
 * vers service-commandes (V4 TP4-openfeign).
 */
@SpringBootApplication
@EnableFeignClients
public class ServiceUsagersApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceUsagersApplication.class, args);
    }
}
