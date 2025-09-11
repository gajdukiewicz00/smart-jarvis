package com.smartjarvis.dm.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartjarvis.dm.model.DialogDecision;
import com.smartjarvis.dm.service.DialogManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka listener for NLU intent events
 * Processes intents and publishes decision events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NLUIntentListener {

    private final DialogManager dialogManager;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "nlu.intent", groupId = "dm-service")
    public void handleIntent(String message) {
        try {
            Map<?,?> payload = objectMapper.readValue(message, Map.class);
            String sessionId = (String) payload.get("sessionId");
            String userId = (String) payload.get("userId");
            String intent = (String) payload.get("intent");
            Double confidence = payload.get("confidence") instanceof Number ? ((Number) payload.get("confidence")).doubleValue() : null;
            Object entities = payload.get("entities");

            log.info("Processing intent: sessionId={}, intent={}, confidence={}", sessionId, intent, confidence);

            // Process intent with dialog manager
            DialogDecision decision = dialogManager.processIntent(
                intent,
                entities,
                sessionId,
                userId
            );

            // Create DM decision event
            Map<String, Object> decisionEvent = new HashMap<>();
            decisionEvent.put("sessionId", decision.getSessionId());
            decisionEvent.put("userId", decision.getUserId());
            decisionEvent.put("action", decision.getAction());
            decisionEvent.put("targetService", decision.getTargetService());
            decisionEvent.put("responseText", decision.getResponseText());
            decisionEvent.put("parameters", decision.getParameters());
            decisionEvent.put("requiresConfirmation", decision.isRequiresConfirmation());
            decisionEvent.put("timestamp", Instant.now().toEpochMilli());

            // Publish decision event
            kafkaTemplate.send("dm.decision", sessionId, decisionEvent);

            log.info("Decision published: sessionId={}, action={}, target={}", 
                    decision.getSessionId(), 
                    decision.getAction(), 
                    decision.getTargetService());

        } catch (Exception e) {
            log.error("Failed to process intent message", e);
        }
    }
}
