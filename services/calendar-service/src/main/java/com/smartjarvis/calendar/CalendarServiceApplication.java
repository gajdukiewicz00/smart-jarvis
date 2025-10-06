package com.smartjarvis.calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SmartJARVIS Calendar Service Application
 * 
 * Calendar and event management microservice with MongoDB storage and Quartz scheduling.
 * Handles events, reminders, recurring appointments, and notifications.
 */
@SpringBootApplication
@EnableKafka
@EnableMongoAuditing
@EnableScheduling
public class CalendarServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalendarServiceApplication.class, args);
    }
}
