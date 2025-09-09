package com.smartjarvis.dm.kafka;

import com.smartjarvis.dm.model.DialogDecision;
import com.smartjarvis.dm.service.DialogManager;
import com.smartjarvis.events.NLUIntentEvent;
import com.smartjarvis.events.DMDecisionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

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

    @KafkaListener(topics = "nlu.intent", groupId = "dm-service")
    public void handleIntent(NLUIntentEvent intentEvent) {
        try {
            log.info("Processing intent: sessionId={}, intent={}, confidence={:.2f}", 
                    intentEvent.getSessionId(), 
                    intentEvent.getIntent(),
                    intentEvent.getConfidence());

            // Process intent with dialog manager
            DialogDecision decision = dialogManager.processIntent(
                intentEvent.getIntent(),
                intentEvent.getEntities(),
                intentEvent.getSessionId(),
                intentEvent.getUserId()
            );

            // Create DM decision event
            DMDecisionEvent decisionEvent = DMDecisionEvent.newBuilder()
                    .setSessionId(decision.getSessionId())
                    .setUserId(decision.getUserId())
                    .setAction(decision.getAction())
                    .setTargetService(decision.getTargetService())
                    .setResponseText(decision.getResponseText())
                    .setParameters(decision.getParameters())
                    .setRequiresConfirmation(decision.isRequiresConfirmation())
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            // Publish decision event
            kafkaTemplate.send("dm.decision", intentEvent.getSessionId(), decisionEvent);

            log.info("Decision published: sessionId={}, action={}, target={}", 
                    decision.getSessionId(), 
                    decision.getAction(), 
                    decision.getTargetService());

        } catch (Exception e) {
            log.error("Failed to process intent: sessionId={}", 
                    intentEvent.getSessionId(), e);
        }
    }
}
