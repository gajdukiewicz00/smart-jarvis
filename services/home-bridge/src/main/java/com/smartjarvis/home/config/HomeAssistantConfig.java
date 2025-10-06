package com.smartjarvis.home.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuration for Home Assistant integration
 */
@Component
@ConfigurationProperties(prefix = "smartjarvis.home-assistant")
@Data
@Slf4j
public class HomeAssistantConfig {

    /**
     * Home Assistant URL
     */
    private String url = "http://localhost:8123";

    /**
     * Long-lived access token
     */
    private String token;

    /**
     * Entity mappings
     */
    private Entities entities = new Entities();

    @Data
    public static class Entities {
        /**
         * Light entities by room
         */
        private Map<String, String> lights = Map.of(
            "гостиная", "light.living_room_main",
            "спальня", "light.bedroom_main", 
            "кухня", "light.kitchen_main",
            "ванная", "light.bathroom_main",
            "коридор", "light.hallway_main"
        );

        /**
         * Media player entities by room
         */
        private Map<String, String> mediaPlayers = Map.of(
            "гостиная", "media_player.living_room_speaker",
            "спальня", "media_player.bedroom_speaker",
            "кухня", "media_player.kitchen_speaker",
            "везде", "media_player.group_all_speakers"
        );

        /**
         * Scene entities by name
         */
        private Map<String, String> scenes = Map.of(
            "фокус", "scene.focus_mode",
            "релакс", "scene.relax_mode",
            "сон", "scene.sleep_mode",
            "работа", "scene.work_mode",
            "кино", "scene.movie_mode",
            "вечеринка", "scene.party_mode"
        );

        /**
         * Switch entities by name
         */
        private Map<String, String> switches = Map.of(
            "компьютер", "switch.pc_power",
            "монитор", "switch.monitor_power",
            "принтер", "switch.printer_power"
        );
    }

    /**
     * Get light entity ID by room name
     */
    public String getLightEntity(String room) {
        String entityId = entities.getLights().get(room.toLowerCase());
        if (entityId == null) {
            log.warn("No light entity found for room: {}", room);
        }
        return entityId;
    }

    /**
     * Get media player entity ID by room name
     */
    public String getMediaPlayerEntity(String room) {
        String entityId = entities.getMediaPlayers().get(room.toLowerCase());
        if (entityId == null) {
            log.warn("No media player entity found for room: {}", room);
        }
        return entityId;
    }

    /**
     * Get scene entity ID by scene name
     */
    public String getSceneEntity(String sceneName) {
        String entityId = entities.getScenes().get(sceneName.toLowerCase());
        if (entityId == null) {
            log.warn("No scene entity found: {}", sceneName);
        }
        return entityId;
    }

    /**
     * Get switch entity ID by switch name
     */
    public String getSwitchEntity(String switchName) {
        String entityId = entities.getSwitches().get(switchName.toLowerCase());
        if (entityId == null) {
            log.warn("No switch entity found: {}", switchName);
        }
        return entityId;
    }

    /**
     * Check if configuration is valid
     */
    public boolean isConfigured() {
        boolean configured = url != null && !url.isEmpty() && 
                           token != null && !token.isEmpty();
        
        if (!configured) {
            log.warn("Home Assistant not properly configured - missing URL or token");
        }
        
        return configured;
    }

    /**
     * Get all configured entities
     */
    public Map<String, Object> getAllEntities() {
        return Map.of(
            "lights", entities.getLights(),
            "media_players", entities.getMediaPlayers(),
            "scenes", entities.getScenes(),
            "switches", entities.getSwitches()
        );
    }
}
