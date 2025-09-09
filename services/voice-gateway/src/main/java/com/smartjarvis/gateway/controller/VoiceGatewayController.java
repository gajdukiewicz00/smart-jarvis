package com.smartjarvis.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for Voice Gateway health checks and status
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class VoiceGatewayController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.debug("Health check requested");
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "voice-gateway", 
            "version", "1.0.0-SNAPSHOT",
            "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        log.debug("Service info requested");
        
        return ResponseEntity.ok(Map.of(
            "service", "voice-gateway",
            "description", "WebSocket gateway for voice communication",
            "version", "1.0.0-SNAPSHOT",
            "architecture", "microservices",
            "capabilities", Map.of(
                "websocket", true,
                "audio-streaming", true,
                "kafka-events", true,
                "metrics", true,
                "tracing", true
            )
        ));
    }
}
