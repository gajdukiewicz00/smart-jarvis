package com.smartjarvis.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * SmartJARVIS Todo Service Application
 * 
 * Task management microservice with MongoDB storage and Kafka integration.
 * Handles task creation, updates, and lifecycle management.
 */
@SpringBootApplication
@EnableKafka
@EnableMongoAuditing
public class TodoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoServiceApplication.class, args);
    }
}
