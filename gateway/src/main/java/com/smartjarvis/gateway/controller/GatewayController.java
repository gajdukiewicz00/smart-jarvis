package com.smartjarvis.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Main Gateway Controller
 * 
 * Provides:
 * - Service information
 * - Health status
 * - API documentation endpoints
 */
@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {

    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @Value("${gateway.security.enabled:false}")
    private boolean securityEnabled;

    /**
     * Gateway information endpoint
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> getGatewayInfo() {
        logger.info("Gateway info requested");
        
        Map<String, Object> info = new HashMap<>();
        info.put("name", applicationName);
        info.put("version", "1.0.0");
        info.put("port", serverPort);
        info.put("timestamp", LocalDateTime.now());
        info.put("securityEnabled", securityEnabled);
        info.put("status", "UP");
        
        Map<String, String> services = new HashMap<>();
        services.put("task-service", "/api/v1/tasks/**");
        services.put("nlp-engine", "/api/v1/nlp/**");
        services.put("speech-service", "/api/v1/speech/**");
        info.put("availableServices", services);
        
        return Mono.just(ResponseEntity.ok(info));
    }

    /**
     * Gateway health check
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> getGatewayHealth() {
        logger.debug("Gateway health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", applicationName);
        health.put("version", "1.0.0");
        
        return Mono.just(ResponseEntity.ok(health));
    }

    /**
     * API documentation endpoint
     */
    @GetMapping("/docs")
    public Mono<ResponseEntity<Map<String, Object>>> getApiDocs() {
        logger.info("API documentation requested");
        
        Map<String, Object> docs = new HashMap<>();
        docs.put("title", "SmartJARVIS API Gateway");
        docs.put("version", "1.0.0");
        docs.put("description", "API Gateway for SmartJARVIS microservices");
        docs.put("timestamp", LocalDateTime.now());
        
        Map<String, Object> endpoints = new HashMap<>();
        
        // Task Service endpoints
        Map<String, String> taskEndpoints = new HashMap<>();
        taskEndpoints.put("GET /api/v1/tasks", "Get all tasks");
        taskEndpoints.put("GET /api/v1/tasks/{id}", "Get task by ID");
        taskEndpoints.put("POST /api/v1/tasks", "Create new task");
        taskEndpoints.put("PUT /api/v1/tasks/{id}", "Update task");
        taskEndpoints.put("DELETE /api/v1/tasks/{id}", "Delete task");
        endpoints.put("task-service", taskEndpoints);
        
        // NLP Engine endpoints
        Map<String, String> nlpEndpoints = new HashMap<>();
        nlpEndpoints.put("POST /api/v1/nlp/process", "Process natural language input");
        nlpEndpoints.put("GET /api/v1/nlp/intents", "Get available intents");
        endpoints.put("nlp-engine", nlpEndpoints);
        
        // Speech Service endpoints
        Map<String, String> speechEndpoints = new HashMap<>();
        speechEndpoints.put("POST /api/v1/speech/recognize", "Speech recognition");
        speechEndpoints.put("POST /api/v1/speech/synthesize", "Text-to-speech");
        endpoints.put("speech-service", speechEndpoints);
        
        docs.put("endpoints", endpoints);
        
        return Mono.just(ResponseEntity.ok(docs));
    }
}