package com.smartjarvis.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for handling service unavailability
 * 
 * Provides fallback responses when microservices are down
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    /**
     * Fallback for Task Service
     */
    @GetMapping("/task-service")
    public Mono<ResponseEntity<Map<String, Object>>> taskServiceFallback() {
        logger.warn("Task Service is unavailable, using fallback response");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Task Service is currently unavailable. Please try again later.");
        response.put("service", "task-service");
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for NLP Engine
     */
    @GetMapping("/nlp-engine")
    public Mono<ResponseEntity<Map<String, Object>>> nlpEngineFallback() {
        logger.warn("NLP Engine is unavailable, using fallback response");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "NLP Engine is currently unavailable. Please try again later.");
        response.put("service", "nlp-engine");
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Speech Service
     */
    @GetMapping("/speech-service")
    public Mono<ResponseEntity<Map<String, Object>>> speechServiceFallback() {
        logger.warn("Speech Service is unavailable, using fallback response");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Speech Service is currently unavailable. Please try again later.");
        response.put("service", "speech-service");
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    /**
     * Fallback for Health Check
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> healthFallback() {
        logger.warn("Health check service is unavailable, using fallback response");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "Health check service is currently unavailable.");
        response.put("service", "health-check");
        response.put("fallback", true);
        response.put("gateway", "UP");
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}