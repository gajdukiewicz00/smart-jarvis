package com.smartjarvis.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * SmartJARVIS Device Agent Application
 * 
 * PC control microservice for Linux systems.
 * Handles volume control, media playback, application launching, and system operations.
 */
@SpringBootApplication
@EnableKafka
public class DeviceAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceAgentApplication.class, args);
    }
}
