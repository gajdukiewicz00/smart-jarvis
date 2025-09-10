package com.smartjarvis.device.kafka;

import com.smartjarvis.device.model.CommandResult;
import com.smartjarvis.device.service.LinuxDeviceService;
import com.smartjarvis.events.DMDecisionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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

    @KafkaListener(topics = "dm.decision", groupId = "device-agent")
    public void handleDecision(DMDecisionEvent decisionEvent) {
        try {
            // Only process decisions for device-agent
            if (!"device-agent".equals(decisionEvent.getTargetService())) {
                return;
            }

            log.info("Processing device decision: sessionId={}, action={}", 
                    decisionEvent.getSessionId(), decisionEvent.getAction());

            CommandResult result = switch (decisionEvent.getAction()) {
                case "set_volume" -> handleVolumeControl(decisionEvent);
                case "media_control" -> handleMediaControl(decisionEvent);
                case "open_app" -> handleOpenApplication(decisionEvent);
                case "open_url" -> handleOpenUrl(decisionEvent);
                case "take_screenshot" -> handleTakeScreenshot(decisionEvent);
                case "lock_screen" -> handleLockScreen(decisionEvent);
                default -> {
                    log.warn("Unknown device action: {}", decisionEvent.getAction());
                    yield CommandResult.builder()
                            .success(false)
                            .errorMessage("Unknown action: " + decisionEvent.getAction())
                            .timestamp(System.currentTimeMillis())
                            .build();
                }
            };

            // Publish result back
            publishDeviceResult(decisionEvent, result);

        } catch (Exception e) {
            log.error("Failed to process device decision: sessionId={}", 
                    decisionEvent.getSessionId(), e);
            
            // Publish error result
            publishDeviceResult(decisionEvent, CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    private CommandResult handleVolumeControl(DMDecisionEvent decision) {
        String volumeStr = decision.getParameters().get("volume");
        String action = decision.getParameters().get("volume_action");
        
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

    private CommandResult handleMediaControl(DMDecisionEvent decision) {
        String action = decision.getParameters().get("media_action");
        if (action == null) {
            action = "play-pause"; // Default action
        }
        
        return deviceService.mediaControl(action);
    }

    private CommandResult handleOpenApplication(DMDecisionEvent decision) {
        String appName = decision.getParameters().get("app_name");
        if (appName == null) {
            throw new IllegalArgumentException("App name not provided");
        }
        
        return deviceService.openApplication(appName);
    }

    private CommandResult handleOpenUrl(DMDecisionEvent decision) {
        String url = decision.getParameters().get("url");
        if (url == null) {
            throw new IllegalArgumentException("URL not provided");
        }
        
        return deviceService.openUrl(url);
    }

    private CommandResult handleTakeScreenshot(DMDecisionEvent decision) {
        return deviceService.takeScreenshot();
    }

    private CommandResult handleLockScreen(DMDecisionEvent decision) {
        return deviceService.lockScreen();
    }

    private void publishDeviceResult(DMDecisionEvent originalDecision, CommandResult result) {
        try {
            // Create device result event (simplified)
            kafkaTemplate.send("device.result", originalDecision.getSessionId(), result);
            
            log.info("Device result published: sessionId={}, success={}", 
                    originalDecision.getSessionId(), result.isSuccess());
                    
        } catch (Exception e) {
            log.error("Failed to publish device result: sessionId={}", 
                    originalDecision.getSessionId(), e);
        }
    }
}
