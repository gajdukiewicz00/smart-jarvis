package com.smartjarvis.nlu.controller;

import com.smartjarvis.nlu.model.IntentResult;
import com.smartjarvis.nlu.service.RuleBasedNLU;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for NLU Service
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class NLUController {

    private final RuleBasedNLU nluService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.debug("Health check requested");
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "nlu-service",
            "version", "1.0.0-SNAPSHOT",
            "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        log.debug("Service info requested");
        
        return ResponseEntity.ok(Map.of(
            "service", "nlu-service",
            "description", "Natural Language Understanding microservice",
            "version", "1.0.0-SNAPSHOT",
            "architecture", "microservices",
            "capabilities", Map.of(
                "intent-recognition", true,
                "entity-extraction", true,
                "rule-based", true,
                "kafka-events", true
            )
        ));
    }

    @PostMapping("/extract-intent")
    public ResponseEntity<IntentResult> extractIntent(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            log.info("Processing direct intent extraction request: '{}'", text);
            
            IntentResult result = nluService.extractIntent(text);
            
            log.info("Intent extracted: {} (confidence: {:.2f})", 
                    result.getIntent(), result.getConfidence());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to extract intent", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
