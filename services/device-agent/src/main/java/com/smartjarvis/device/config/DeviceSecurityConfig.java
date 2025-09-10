package com.smartjarvis.device.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Security configuration for device agent
 * Controls which commands and applications are allowed
 */
@Component
@ConfigurationProperties(prefix = "smartjarvis.device-agent.security")
@Data
@Slf4j
public class DeviceSecurityConfig {

    /**
     * Whether security is enabled
     */
    private boolean enabled = true;

    /**
     * Maximum volume level allowed
     */
    private int maxVolume = 100;

    /**
     * Screenshot storage path
     */
    private String screenshotPath = "/tmp/smartjarvis/screenshots";

    /**
     * Allowed applications
     */
    private List<String> allowedApps = List.of(
        "code", "vscode",
        "firefox", "chrome", "google-chrome",
        "terminal", "gnome-terminal",
        "files", "nautilus",
        "calculator", "gnome-calculator",
        "settings", "gnome-control-center"
    );

    /**
     * Allowed volume commands
     */
    private List<String> allowedVolumeCommands = List.of(
        "pactl", "amixer"
    );

    /**
     * Allowed media actions
     */
    private List<String> allowedMediaActions = List.of(
        "play", "pause", "play-pause", "next", "previous", "stop"
    );

    /**
     * Allowed system commands (base commands)
     */
    private List<String> allowedSystemCommands = List.of(
        "gnome-screenshot", "scrot", "import",
        "gnome-screensaver-command", "loginctl", "dm-tool", "xdg-screensaver",
        "xdg-open", "gtk-launch",
        "uname", "lsb_release", "cat"
    );

    /**
     * Dangerous commands that are explicitly forbidden
     */
    private List<String> forbiddenCommands = List.of(
        "rm", "rmdir", "sudo", "su", "chmod", "chown",
        "dd", "mkfs", "fdisk", "mount", "umount",
        "systemctl", "service", "killall", "pkill",
        "passwd", "useradd", "userdel", "groupadd", "groupdel",
        "iptables", "ufw", "firewall-cmd",
        "crontab", "at", "batch"
    );

    /**
     * Check if command is allowed
     */
    public boolean isCommandAllowed(String command) {
        if (!enabled) {
            return true; // Security disabled
        }

        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        String normalizedCommand = command.toLowerCase().trim();
        
        // Check for forbidden commands
        for (String forbidden : forbiddenCommands) {
            if (normalizedCommand.contains(forbidden)) {
                log.warn("Forbidden command detected: {}", command);
                return false;
            }
        }

        // Check if base command is in allowed list
        String baseCommand = extractBaseCommand(normalizedCommand);
        boolean allowed = allowedSystemCommands.contains(baseCommand) ||
                         allowedVolumeCommands.contains(baseCommand) ||
                         baseCommand.equals("playerctl");

        if (!allowed) {
            log.warn("Command not in allowlist: {}", command);
        }

        return allowed;
    }

    /**
     * Extract base command from full command string
     */
    private String extractBaseCommand(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length > 0) {
            return parts[0];
        }
        return command;
    }

    /**
     * Check if application is allowed
     */
    public boolean isAppAllowed(String appName) {
        if (!enabled) {
            return true;
        }
        
        return allowedApps.contains(appName.toLowerCase());
    }

    /**
     * Get safe screenshot path
     */
    public String getSafeScreenshotPath() {
        // Ensure path is within allowed directory
        if (!screenshotPath.startsWith("/tmp/") && !screenshotPath.startsWith("/home/")) {
            return "/tmp/smartjarvis/screenshots";
        }
        return screenshotPath;
    }

    /**
     * Log security event
     */
    public void logSecurityEvent(String event, String details) {
        log.warn("SECURITY EVENT: {} - {}", event, details);
        // TODO: Send to audit service
    }
}
