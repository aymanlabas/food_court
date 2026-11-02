package com.foodcourt.usagers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceUsagersApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceUsagersApplication.class, args);
    }
}
