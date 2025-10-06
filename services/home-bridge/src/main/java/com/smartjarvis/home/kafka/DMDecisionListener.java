package com.smartjarvis.home.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "dm.decision", groupId = "home-bridge")
    public void handleDecision(String message) {
        try {
            Map<?,?> decisionEvent = objectMapper.readValue(message, Map.class);
            // Only process decisions for home-bridge
            if (!"home-bridge".equals(decisionEvent.get("targetService"))) {
                return;
            }

            String sessionId = (String) decisionEvent.get("sessionId");
            String action = (String) decisionEvent.get("action");
            @SuppressWarnings("unchecked")
            Map<String,String> parameters = (Map<String,String>) decisionEvent.get("parameters");

            log.info("Processing home decision: sessionId={}, action={}", sessionId, action);

            HomeCommandResult result = switch (action) {
                case "control_light" -> handleLightControl(parameters);
                case "control_media" -> handleMediaControl(parameters);
                case "activate_scene" -> handleSceneActivation(parameters);
                default -> {
                    log.warn("Unknown home action: {}", action);
                    yield HomeCommandResult.builder()
                            .success(false)
                            .errorMessage("Unknown action: " + action)
                            .timestamp(System.currentTimeMillis())
                            .build();
                }
            };

            // Publish result back
            publishHomeResult(sessionId, result);

        } catch (Exception e) {
            log.error("Failed to process home decision message", e);
            
            // Publish error result
            publishHomeResult(null, HomeCommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    private HomeCommandResult handleLightControl(Map<String,String> parameters) {
        String room = parameters.get("location");
        String action = parameters.get("device_action");
        String brightnessStr = parameters.get("brightness");
        
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

    private HomeCommandResult handleMediaControl(Map<String,String> parameters) {
        String room = parameters.get("location");
        String action = parameters.get("media_action");
        String volumeStr = parameters.get("volume_level");
        
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

    private HomeCommandResult handleSceneActivation(Map<String,String> parameters) {
        String sceneName = parameters.get("scene_name");
        
        if (sceneName == null) {
            sceneName = parameters.get("scene");
        }
        
        if (sceneName == null) {
            throw new IllegalArgumentException("Scene name not provided");
        }

        return homeService.activateScene(sceneName);
    }

    private void publishHomeResult(String sessionId, HomeCommandResult result) {
        try {
            // Create home result event (simplified)
            kafkaTemplate.send("home.result", sessionId, result);
            
            log.info("Home result published: sessionId={}, success={}", 
                    sessionId, result.isSuccess());
                    
        } catch (Exception e) {
            log.error("Failed to publish home result: sessionId={}", sessionId, e);
        }
    }
}
