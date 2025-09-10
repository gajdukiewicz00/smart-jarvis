package com.smartjarvis.home.kafka;

import com.smartjarvis.events.DMDecisionEvent;
import com.smartjarvis.home.model.HomeCommandResult;
import com.smartjarvis.home.service.HomeAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka listener for DM decision events
 * Processes home automation decisions from Dialog Manager
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DMDecisionListener {

    private final HomeAssistantService homeService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "dm.decision", groupId = "home-bridge")
    public void handleDecision(DMDecisionEvent decisionEvent) {
        try {
            // Only process decisions for home-bridge
            if (!"home-bridge".equals(decisionEvent.getTargetService())) {
                return;
            }

            log.info("Processing home decision: sessionId={}, action={}", 
                    decisionEvent.getSessionId(), decisionEvent.getAction());

            HomeCommandResult result = switch (decisionEvent.getAction()) {
                case "control_light" -> handleLightControl(decisionEvent);
                case "control_media" -> handleMediaControl(decisionEvent);
                case "activate_scene" -> handleSceneActivation(decisionEvent);
                default -> {
                    log.warn("Unknown home action: {}", decisionEvent.getAction());
                    yield HomeCommandResult.builder()
                            .success(false)
                            .errorMessage("Unknown action: " + decisionEvent.getAction())
                            .timestamp(System.currentTimeMillis())
                            .build();
                }
            };

            // Publish result back
            publishHomeResult(decisionEvent, result);

        } catch (Exception e) {
            log.error("Failed to process home decision: sessionId={}", 
                    decisionEvent.getSessionId(), e);
            
            // Publish error result
            publishHomeResult(decisionEvent, HomeCommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    private HomeCommandResult handleLightControl(DMDecisionEvent decision) {
        String room = decision.getParameters().get("location");
        String action = decision.getParameters().get("device_action");
        String brightnessStr = decision.getParameters().get("brightness");
        
        if (room == null) {
            room = "гостиная"; // Default room
        }
        
        if (action == null) {
            action = "toggle"; // Default action
        }

        Map<String, Object> params = Map.of();
        if (brightnessStr != null) {
            try {
                int brightness = Integer.parseInt(brightnessStr);
                params = Map.of("brightness", brightness);
            } catch (NumberFormatException e) {
                log.warn("Invalid brightness value: {}", brightnessStr);
            }
        }

        return homeService.controlLight(room, action, params);
    }

    private HomeCommandResult handleMediaControl(DMDecisionEvent decision) {
        String room = decision.getParameters().get("location");
        String action = decision.getParameters().get("media_action");
        String volumeStr = decision.getParameters().get("volume_level");
        
        if (room == null) {
            room = "гостиная"; // Default room
        }
        
        if (action == null) {
            action = "play_pause"; // Default action
        }

        Map<String, Object> params = Map.of();
        if (volumeStr != null) {
            try {
                int volume = Integer.parseInt(volumeStr);
                params = Map.of("volume_level", volume);
            } catch (NumberFormatException e) {
                log.warn("Invalid volume value: {}", volumeStr);
            }
        }

        return homeService.controlMediaPlayer(room, action, params);
    }

    private HomeCommandResult handleSceneActivation(DMDecisionEvent decision) {
        String sceneName = decision.getParameters().get("scene_name");
        
        if (sceneName == null) {
            sceneName = decision.getParameters().get("scene");
        }
        
        if (sceneName == null) {
            throw new IllegalArgumentException("Scene name not provided");
        }

        return homeService.activateScene(sceneName);
    }

    private void publishHomeResult(DMDecisionEvent originalDecision, HomeCommandResult result) {
        try {
            // Create home result event (simplified)
            kafkaTemplate.send("home.result", originalDecision.getSessionId(), result);
            
            log.info("Home result published: sessionId={}, success={}", 
                    originalDecision.getSessionId(), result.isSuccess());
                    
        } catch (Exception e) {
            log.error("Failed to publish home result: sessionId={}", 
                    originalDecision.getSessionId(), e);
        }
    }
}
