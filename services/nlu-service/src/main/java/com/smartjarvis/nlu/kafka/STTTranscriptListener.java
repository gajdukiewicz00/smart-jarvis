package com.smartjarvis.nlu.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartjarvis.nlu.model.IntentResult;
import com.smartjarvis.nlu.service.RuleBasedNLU;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka listener for STT transcript events
 * Processes transcripts and publishes intent events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class STTTranscriptListener {

    private final RuleBasedNLU nluService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "stt.transcript", groupId = "nlu-service")
    public void handleTranscript(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String sessionId = node.path("sessionId").asText(null);
            String userId = node.path("userId").asText(null);
            String transcript = node.path("transcript").asText(null);

            log.info("Processing transcript: sessionId={}, text='{}'", sessionId, transcript);

            // Extract intent from transcript
            IntentResult intentResult = nluService.extractIntent(transcript);

            // Build generic intent payload (JSON)
            Map<String, Object> intentEvent = new HashMap<>();
            intentEvent.put("sessionId", sessionId);
            intentEvent.put("userId", userId);
            intentEvent.put("intent", intentResult.getIntent());
            intentEvent.put("confidence", intentResult.getConfidence());
            intentEvent.put("entities", intentResult.getEntities());
            intentEvent.put("originalText", transcript);
            intentEvent.put("timestamp", Instant.now().toEpochMilli());

            // Publish intent event
            kafkaTemplate.send("nlu.intent", sessionId, intentEvent);

            log.info("Intent published: sessionId={}, intent={}, confidence={}", 
                    sessionId, intentResult.getIntent(), intentResult.getConfidence());

        } catch (Exception e) {
            log.error("Failed to process transcript message", e);
        }
    }
}
