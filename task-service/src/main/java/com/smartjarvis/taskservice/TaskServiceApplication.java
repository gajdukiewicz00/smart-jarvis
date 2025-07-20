package com.smartjarvis.taskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Boot application for Task Service
 */
@SpringBootApplication(scanBasePackages = "com.smartjarvis.taskservice")
@EnableScheduling
@RestController
public class TaskServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
    
    @GetMapping("/api/v1/test")
    public String test() {
        return "Task Service is working!";
    }
    
    @GetMapping("/api/v1/tasks-simple")
    public String tasksSimple() {
        return "Tasks endpoint is working!";
    }
} 