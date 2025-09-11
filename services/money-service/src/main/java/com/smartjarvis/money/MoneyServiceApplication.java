package com.smartjarvis.money;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * SmartJARVIS Money Service Application
 * 
 * Financial tracking microservice with MongoDB storage and advanced analytics.
 * Handles transactions, budgets, categories, and financial reporting.
 */
@SpringBootApplication
@EnableKafka
@EnableMongoAuditing
public class MoneyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoneyServiceApplication.class, args);
    }
}
