package com.smartjarvis.device.controller;

import com.smartjarvis.device.model.CommandResult;
import com.smartjarvis.device.service.LinuxDeviceService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for device operations
 */
@RestController
@RequestMapping("/api/v1/device")
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final LinuxDeviceService deviceService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "device-agent",
            "version", "1.0.0-SNAPSHOT",
            "platform", "linux",
            "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        try {
            CommandResult systemInfo = deviceService.getSystemInfo();
            
            return ResponseEntity.ok(Map.of(
                "service", "device-agent",
                "description", "PC control microservice for Linux systems",
                "version", "1.0.0-SNAPSHOT",
                "platform", "linux",
                "capabilities", Map.of(
                    "volume-control", true,
                    "media-control", true,
                    "app-launching", true,
                    "screenshots", true,
                    "screen-lock", true,
                    "url-opening", true
                ),
                "system", systemInfo.getOutput()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "service", "device-agent",
                "status", "error",
                "error", e.getMessage()
            ));
        }
    }

    // Volume Control
    @PostMapping("/volume")
    public ResponseEntity<CommandResult> setVolume(@RequestParam @Min(0) @Max(100) int level) {
        log.info("Setting volume to {}%", level);
        
        try {
            CommandResult result = deviceService.setVolume(level);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to set volume", e);
            return ResponseEntity.badRequest().body(
                CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build()
            );
        }
    }

    @GetMapping("/volume")
    public ResponseEntity<Map<String, Object>> getVolume() {
        try {
            int currentVolume = deviceService.getCurrentVolume();
            return ResponseEntity.ok(Map.of(
                "volume", currentVolume,
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // Media Control
    @PostMapping("/media/{action}")
    public ResponseEntity<CommandResult> mediaControl(@PathVariable String action) {
        log.info("Media control: {}", action);
        
        try {
            CommandResult result = deviceService.mediaControl(action);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to control media: {}", action, e);
            return ResponseEntity.badRequest().body(
                CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build()
            );
        }
    }

    // Application Control
    @PostMapping("/app/{name}")
    public ResponseEntity<CommandResult> openApp(@PathVariable String name) {
        log.info("Opening application: {}", name);
        
        try {
            CommandResult result = deviceService.openApplication(name);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to open application: {}", name, e);
            return ResponseEntity.badRequest().body(
                CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build()
            );
        }
    }

    @PostMapping("/url")
    public ResponseEntity<CommandResult> openUrl(@RequestParam String url) {
        log.info("Opening URL: {}", url);
        
        try {
            CommandResult result = deviceService.openUrl(url);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to open URL: {}", url, e);
            return ResponseEntity.badRequest().body(
                CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build()
            );
        }
    }

    // Screenshot
    @PostMapping("/screenshot")
    public ResponseEntity<CommandResult> takeScreenshot() {
        log.info("Taking screenshot");
        
        try {
            CommandResult result = deviceService.takeScreenshot();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to take screenshot", e);
            return ResponseEntity.badRequest().body(
                CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build()
            );
        }
    }

    // System Control
    @PostMapping("/lock")
    public ResponseEntity<CommandResult> lockScreen() {
        log.info("Locking screen");
        
        try {
            CommandResult result = deviceService.lockScreen();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to lock screen", e);
            return ResponseEntity.badRequest().body(
                CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build()
            );
        }
    }

    @GetMapping("/system")
    public ResponseEntity<CommandResult> getSystemInfo() {
        try {
            CommandResult result = deviceService.getSystemInfo();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                CommandResult.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build()
            );
        }
    }
}
