package com.smartjarvis.home;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * SmartJARVIS Home Bridge Application
 * 
 * Home Assistant integration microservice.
 * Handles smart home device control, scenes, and automation.
 */
@SpringBootApplication
@EnableKafka
@ConfigurationPropertiesScan
public class HomeBridgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomeBridgeApplication.class, args);
    }
}
