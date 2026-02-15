package com.example.sim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main Spring Boot Application Entry Point
 * 
 * This is the starting point of the entire microservice
 */
@SpringBootApplication
@EnableConfigurationProperties
public class SimMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimMicroserviceApplication.class, args);
    }
}