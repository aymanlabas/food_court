package com.foodcourt.service_commandes.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignResilienceConfig {

    @Bean
    public Retryer feignRetryer() {
        // Réessaie jusqu'à 3 fois (intervalle initial 200ms, max 1s) en cas d'erreur réseau/timeout
        return new Retryer.Default(200, TimeUnit.SECONDS.toMillis(1), 3);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}