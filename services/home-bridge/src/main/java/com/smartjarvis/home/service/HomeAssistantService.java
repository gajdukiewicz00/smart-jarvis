package com.smartjarvis.home.service;

import com.smartjarvis.home.client.HomeAssistantClient;
import com.smartjarvis.home.config.HomeAssistantConfig;
import com.smartjarvis.home.exception.HomeControlException;
import com.smartjarvis.home.model.HomeCommandResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Home Assistant integration service
 * Handles smart home device control through HA API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HomeAssistantService {

    private final HomeAssistantClient haClient;
    private final HomeAssistantConfig haConfig;

    /**
     * Control light (on/off/brightness/color)
     */
    public HomeCommandResult controlLight(String room, String action, Map<String, Object> params) {
        try {
            String entityId = haConfig.getLightEntity(room);
            if (entityId == null) {
                throw new HomeControlException("No light entity found for room: " + room);
            }

            log.info("Controlling light: room={}, action={}, entity={}", room, action, entityId);

            switch (action.toLowerCase()) {
                case "turn_on", "включи", "включить" -> {
                    Map<String, Object> data = Map.of();
                    if (params.containsKey("brightness")) {
                        data = Map.of("brightness_pct", params.get("brightness"));
                    }
                    haClient.callService("light", "turn_on", entityId, data);
                }
                case "turn_off", "выключи", "выключить" -> {
                    haClient.callService("light", "turn_off", entityId, Map.of());
                }
                case "toggle", "переключи" -> {
                    haClient.callService("light", "toggle", entityId, Map.of());
                }
                default -> throw new HomeControlException("Unknown light action: " + action);
            }

            return HomeCommandResult.builder()
                    .success(true)
                    .action("light_control")
                    .entityId(entityId)
                    .message("Light " + action + " in " + room)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Failed to control light: room={}, action={}", room, action, e);
            return HomeCommandResult.builder()
                    .success(false)
                    .action("light_control")
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * Control media player (play/pause/volume)
     */
    public HomeCommandResult controlMediaPlayer(String room, String action, Map<String, Object> params) {
        try {
            String entityId = haConfig.getMediaPlayerEntity(room);
            if (entityId == null) {
                throw new HomeControlException("No media player entity found for room: " + room);
            }

            log.info("Controlling media player: room={}, action={}, entity={}", room, action, entityId);

            switch (action.toLowerCase()) {
                case "play", "плей", "воспроизведение" -> {
                    haClient.callService("media_player", "media_play", entityId, Map.of());
                }
                case "pause", "пауза" -> {
                    haClient.callService("media_player", "media_pause", entityId, Map.of());
                }
                case "play_pause", "переключи" -> {
                    haClient.callService("media_player", "media_play_pause", entityId, Map.of());
                }
                case "stop", "стоп" -> {
                    haClient.callService("media_player", "media_stop", entityId, Map.of());
                }
                case "volume_up", "громче" -> {
                    haClient.callService("media_player", "volume_up", entityId, Map.of());
                }
                case "volume_down", "тише" -> {
                    haClient.callService("media_player", "volume_down", entityId, Map.of());
                }
                case "volume_set", "громкость" -> {
                    Object volume = params.get("volume_level");
                    if (volume != null) {
                        // Convert percentage to 0-1 range
                        float volumeLevel = Float.parseFloat(volume.toString()) / 100.0f;
                        haClient.callService("media_player", "volume_set", entityId, 
                                Map.of("volume_level", volumeLevel));
                    }
                }
                default -> throw new HomeControlException("Unknown media action: " + action);
            }

            return HomeCommandResult.builder()
                    .success(true)
                    .action("media_control")
                    .entityId(entityId)
                    .message("Media " + action + " in " + room)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Failed to control media player: room={}, action={}", room, action, e);
            return HomeCommandResult.builder()
                    .success(false)
                    .action("media_control")
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * Activate scene
     */
    public HomeCommandResult activateScene(String sceneName) {
        try {
            String entityId = haConfig.getSceneEntity(sceneName);
            if (entityId == null) {
                throw new HomeControlException("No scene entity found: " + sceneName);
            }

            log.info("Activating scene: name={}, entity={}", sceneName, entityId);

            haClient.callService("scene", "turn_on", entityId, Map.of());

            return HomeCommandResult.builder()
                    .success(true)
                    .action("scene_activation")
                    .entityId(entityId)
                    .message("Scene " + sceneName + " activated")
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("Failed to activate scene: {}", sceneName, e);
            return HomeCommandResult.builder()
                    .success(false)
                    .action("scene_activation")
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * Get entity state
     */
    public Map<String, Object> getEntityState(String entityId) {
        try {
            return haClient.getEntityState(entityId);
        } catch (Exception e) {
            log.error("Failed to get entity state: {}", entityId, e);
            throw new HomeControlException("Failed to get entity state: " + e.getMessage());
        }
    }

    /**
     * Check if Home Assistant is available
     */
    public boolean isHomeAssistantAvailable() {
        try {
            return haClient.checkConnection();
        } catch (Exception e) {
            log.warn("Home Assistant not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get all available entities
     */
    public Map<String, Object> getAvailableEntities() {
        try {
            return haClient.getStates();
        } catch (Exception e) {
            log.error("Failed to get available entities", e);
            throw new HomeControlException("Failed to get available entities: " + e.getMessage());
        }
    }
}
