package com.smartjarvis.dm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * SmartJARVIS Dialog Management Service Application
 * 
 * Dialog Management microservice that processes intents and makes decisions
 * about actions to be performed by other services.
 */
@SpringBootApplication
@EnableKafka
public class DMServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DMServiceApplication.class, args);
    }
}
