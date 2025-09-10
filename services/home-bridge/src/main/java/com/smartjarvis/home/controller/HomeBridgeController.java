package com.smartjarvis.home.controller;

import com.smartjarvis.home.model.HomeCommandResult;
import com.smartjarvis.home.service.HomeAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for home automation operations
 */
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Slf4j
public class HomeBridgeController {

    private final HomeAssistantService homeService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean haAvailable = homeService.isHomeAssistantAvailable();
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "home-bridge",
            "version", "1.0.0-SNAPSHOT",
            "home_assistant", haAvailable ? "CONNECTED" : "DISCONNECTED",
            "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "service", "home-bridge",
            "description", "Home Assistant integration microservice",
            "version", "1.0.0-SNAPSHOT",
            "capabilities", Map.of(
                "light-control", true,
                "media-control", true,
                "scene-activation", true,
                "entity-state", true
            )
        ));
    }

    // Light Control
    @PostMapping("/light/{room}/{action}")
    public ResponseEntity<HomeCommandResult> controlLight(
            @PathVariable String room,
            @PathVariable String action,
            @RequestParam(required = false) Integer brightness) {
        
        log.info("Light control: room={}, action={}, brightness={}", room, action, brightness);
        
        Map<String, Object> params = Map.of();
        if (brightness != null) {
            params = Map.of("brightness", brightness);
        }
        
        HomeCommandResult result = homeService.controlLight(room, action, params);
        return ResponseEntity.ok(result);
    }

    // Media Control
    @PostMapping("/media/{room}/{action}")
    public ResponseEntity<HomeCommandResult> controlMedia(
            @PathVariable String room,
            @PathVariable String action,
            @RequestParam(required = false) Integer volume) {
        
        log.info("Media control: room={}, action={}, volume={}", room, action, volume);
        
        Map<String, Object> params = Map.of();
        if (volume != null) {
            params = Map.of("volume_level", volume);
        }
        
        HomeCommandResult result = homeService.controlMediaPlayer(room, action, params);
        return ResponseEntity.ok(result);
    }

    // Scene Control
    @PostMapping("/scene/{name}")
    public ResponseEntity<HomeCommandResult> activateScene(@PathVariable String name) {
        log.info("Activating scene: {}", name);
        
        HomeCommandResult result = homeService.activateScene(name);
        return ResponseEntity.ok(result);
    }

    // Entity State
    @GetMapping("/entity/{entityId}/state")
    public ResponseEntity<Map<String, Object>> getEntityState(@PathVariable String entityId) {
        try {
            Map<String, Object> state = homeService.getEntityState(entityId);
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // Available Entities
    @GetMapping("/entities")
    public ResponseEntity<Map<String, Object>> getAvailableEntities() {
        try {
            Map<String, Object> entities = homeService.getAvailableEntities();
            return ResponseEntity.ok(entities);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // Connection Test
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        boolean connected = homeService.isHomeAssistantAvailable();
        
        return ResponseEntity.ok(Map.of(
            "connected", connected,
            "timestamp", System.currentTimeMillis()
        ));
    }
}
