package com.foodcourt.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${routes.service-menu-url}")
    private String serviceMenuUrl;

    @Value("${routes.service-commandes-url}")
    private String serviceCommandesUrl;

    @Value("${routes.service-usagers-url}")
    private String serviceUsagersUrl;

    @Value("${routes.service-notification-url}")
    private String serviceNotificationUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ── Menu Service Routes ──
                .route("dishes-route", r -> r
                        .path("/api/dishes/**")
                        .uri(serviceMenuUrl))

                .route("ingredients-route", r -> r
                        .path("/api/ingredients/**")
                        .uri(serviceMenuUrl))

                .route("recipe-lines-route", r -> r
                        .path("/api/recipe-lines/**")
                        .uri(serviceMenuUrl))

                .route("internal-menu-route", r -> r
                        .path("/api/internal/menu/**")
                        .uri(serviceMenuUrl))

                // ── Commandes Service Routes (V4) ──
                .route("orders-route", r -> r
                        .path("/api/orders/**")
                        .uri(serviceCommandesUrl))

                // ── Usagers Service Routes ──
                .route("auth-route", r -> r
                        .path("/api/auth/**")
                        .uri(serviceUsagersUrl))

                .route("users-route", r -> r
                        .path("/api/users/**")
                        .uri(serviceUsagersUrl))

                .route("payments-route", r -> r
                        .path("/api/payments/**")
                        .uri(serviceUsagersUrl))

                .route("internal-usagers-route", r -> r
                        .path("/api/internal/usagers/**")
                        .uri(serviceUsagersUrl))

                // ── Notification Service Routes (V5) ──
                .route("notifications-route", r -> r
                        .path("/api/notifications/**")
                        .uri(serviceNotificationUrl))

                .build();
    }
}
