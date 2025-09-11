package com.smartjarvis.device.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartjarvis.device.model.CommandResult;
import com.smartjarvis.device.service.LinuxDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Kafka listener for DM decision events
 * Processes device control decisions from Dialog Manager
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DMDecisionListener {

    private final LinuxDeviceService deviceService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "dm.decision", groupId = "device-agent")
    public void handleDecision(String message) {
        try {
            Map<?,?> decisionEvent = objectMapper.readValue(message, Map.class);
            // Only process decisions for device-agent
            if (!"device-agent".equals(decisionEvent.get("targetService"))) {
                return;
            }

            String sessionId = (String) decisionEvent.get("sessionId");
            String action = (String) decisionEvent.get("action");
            @SuppressWarnings("unchecked")
            Map<String,String> parameters = (Map<String,String>) decisionEvent.get("parameters");
            
            log.info("Processing device decision: sessionId={}, action={}", sessionId, action);

            CommandResult result = switch (action) {
                case "set_volume" -> handleVolumeControl(parameters);
                case "media_control" -> handleMediaControl(parameters);
                case "open_app" -> handleOpenApplication(parameters);
                case "open_url" -> handleOpenUrl(parameters);
                case "take_screenshot" -> handleTakeScreenshot();
                case "lock_screen" -> handleLockScreen();
                default -> {
                    log.warn("Unknown device action: {}", action);
                    yield CommandResult.builder()
                            .success(false)
                            .errorMessage("Unknown action: " + action)
                            .timestamp(System.currentTimeMillis())
                            .build();
                }
            };

            // Publish result back
            publishDeviceResult(sessionId, result);

        } catch (Exception e) {
            log.error("Failed to process device decision message", e);
            
            // Publish error result
            publishDeviceResult(null, CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    private CommandResult handleVolumeControl(Map<String,String> parameters) {
        String volumeStr = parameters.get("volume");
        String action = parameters.get("volume_action");
        
        try {
            if (volumeStr != null) {
                int volume = Integer.parseInt(volumeStr);
                return deviceService.setVolume(volume);
            } else if ("up".equals(action) || "громче".equals(action)) {
                int currentVolume = deviceService.getCurrentVolume();
                return deviceService.setVolume(Math.min(100, currentVolume + 10));
            } else if ("down".equals(action) || "тише".equals(action)) {
                int currentVolume = deviceService.getCurrentVolume();
                return deviceService.setVolume(Math.max(0, currentVolume - 10));
            } else {
                throw new IllegalArgumentException("Invalid volume parameters");
            }
        } catch (Exception e) {
            throw new RuntimeException("Volume control failed: " + e.getMessage(), e);
        }
    }

    private CommandResult handleMediaControl(Map<String,String> parameters) {
        String action = parameters.get("media_action");
        if (action == null) {
            action = "play-pause"; // Default action
        }
        
        return deviceService.mediaControl(action);
    }

    private CommandResult handleOpenApplication(Map<String,String> parameters) {
        String appName = parameters.get("app_name");
        if (appName == null) {
            throw new IllegalArgumentException("App name not provided");
        }
        
        return deviceService.openApplication(appName);
    }

    private CommandResult handleOpenUrl(Map<String,String> parameters) {
        String url = parameters.get("url");
        if (url == null) {
            throw new IllegalArgumentException("URL not provided");
        }
        
        return deviceService.openUrl(url);
    }

    private CommandResult handleTakeScreenshot() {
        return deviceService.takeScreenshot();
    }

    private CommandResult handleLockScreen() {
        return deviceService.lockScreen();
    }

    private void publishDeviceResult(String sessionId, CommandResult result) {
        try {
            // Create device result event (simplified)
            kafkaTemplate.send("device.result", sessionId, result);
            
            log.info("Device result published: sessionId={}, success={}", 
                    sessionId, result.isSuccess());
                    
        } catch (Exception e) {
            log.error("Failed to publish device result: sessionId={}", sessionId, e);
        }
    }
}
