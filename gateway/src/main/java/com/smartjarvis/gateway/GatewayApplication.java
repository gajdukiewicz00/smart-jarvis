package com.smartjarvis.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Spring Cloud Gateway Application for SmartJARVIS
 * 
 * This gateway provides:
 * - API routing to microservices
 * - Rate limiting
 * - Circuit breaker patterns
 * - Security and authentication
 * - Monitoring and metrics
 * - Request/Response transformation
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}