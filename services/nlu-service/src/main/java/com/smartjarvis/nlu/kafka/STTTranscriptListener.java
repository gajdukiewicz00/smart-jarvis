package com.smartjarvis.nlu.kafka;

import com.smartjarvis.events.STTTranscriptEvent;
import com.smartjarvis.events.NLUIntentEvent;
import com.smartjarvis.nlu.model.IntentResult;
import com.smartjarvis.nlu.service.RuleBasedNLU;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

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

    @KafkaListener(topics = "stt.transcript", groupId = "nlu-service")
    public void handleTranscript(STTTranscriptEvent transcriptEvent) {
        try {
            log.info("Processing transcript: sessionId={}, text='{}'", 
                    transcriptEvent.getSessionId(), transcriptEvent.getTranscript());

            // Extract intent from transcript
            IntentResult intentResult = nluService.extractIntent(transcriptEvent.getTranscript());

            // Create NLU intent event
            NLUIntentEvent intentEvent = NLUIntentEvent.newBuilder()
                    .setSessionId(transcriptEvent.getSessionId())
                    .setUserId(transcriptEvent.getUserId())
                    .setIntent(intentResult.getIntent())
                    .setConfidence(intentResult.getConfidence())
                    .setEntities(intentResult.getEntities())
                    .setOriginalText(transcriptEvent.getTranscript())
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            // Publish intent event
            kafkaTemplate.send("nlu.intent", transcriptEvent.getSessionId(), intentEvent);

            log.info("Intent published: sessionId={}, intent={}, confidence={:.2f}", 
                    transcriptEvent.getSessionId(), 
                    intentResult.getIntent(), 
                    intentResult.getConfidence());

        } catch (Exception e) {
            log.error("Failed to process transcript: sessionId={}", 
                    transcriptEvent.getSessionId(), e);
        }
    }
}
