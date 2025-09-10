package com.smartjarvis.device.service;

import com.smartjarvis.device.config.DeviceSecurityConfig;
import com.smartjarvis.device.exception.DeviceCommandException;
import com.smartjarvis.device.model.CommandResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Linux device control service
 * Handles PC operations through system commands
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LinuxDeviceService {

    private final DeviceSecurityConfig securityConfig;

    /**
     * Set system volume
     */
    public CommandResult setVolume(int percentage) {
        if (percentage < 0 || percentage > securityConfig.getMaxVolume()) {
            throw new DeviceCommandException("Volume must be between 0 and " + securityConfig.getMaxVolume());
        }

        log.info("Setting volume to {}%", percentage);
        
        try {
            // Try PulseAudio first (modern Linux)
            String command = "pactl set-sink-volume @DEFAULT_SINK@ " + percentage + "%";
            CommandResult result = executeCommand(command);
            
            if (result.isSuccess()) {
                return result;
            }
            
            // Fallback to ALSA
            command = "amixer set Master " + percentage + "%";
            return executeCommand(command);
            
        } catch (Exception e) {
            log.error("Failed to set volume", e);
            throw new DeviceCommandException("Failed to set volume: " + e.getMessage());
        }
    }

    /**
     * Get current volume level
     */
    public int getCurrentVolume() {
        try {
            // Get PulseAudio volume
            CommandResult result = executeCommand("pactl get-sink-volume @DEFAULT_SINK@");
            if (result.isSuccess()) {
                String output = result.getOutput();
                // Parse volume from output (simplified)
                if (output.contains("%")) {
                    String[] parts = output.split("%");
                    if (parts.length > 0) {
                        String volumePart = parts[0];
                        String[] volumeWords = volumePart.split("\\s+");
                        for (String word : volumeWords) {
                            try {
                                return Integer.parseInt(word);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
            
            // Fallback to ALSA
            result = executeCommand("amixer get Master | grep -o '[0-9]*%' | head -1 | tr -d '%'");
            if (result.isSuccess()) {
                return Integer.parseInt(result.getOutput().trim());
            }
            
            return 50; // Default fallback
            
        } catch (Exception e) {
            log.warn("Failed to get current volume", e);
            return 50; // Default fallback
        }
    }

    /**
     * Media control operations
     */
    public CommandResult mediaControl(String action) {
        if (!securityConfig.getAllowedMediaActions().contains(action)) {
            throw new DeviceCommandException("Media action not allowed: " + action);
        }

        log.info("Media control: {}", action);

        try {
            String command = switch (action.toLowerCase()) {
                case "play" -> "playerctl play";
                case "pause" -> "playerctl pause";
                case "play-pause" -> "playerctl play-pause";
                case "next" -> "playerctl next";
                case "previous" -> "playerctl previous";
                case "stop" -> "playerctl stop";
                default -> throw new DeviceCommandException("Unknown media action: " + action);
            };

            return executeCommand(command);
            
        } catch (Exception e) {
            log.error("Failed to control media: {}", action, e);
            throw new DeviceCommandException("Failed to control media: " + e.getMessage());
        }
    }

    /**
     * Open application
     */
    public CommandResult openApplication(String appName) {
        if (!securityConfig.getAllowedApps().contains(appName.toLowerCase())) {
            throw new DeviceCommandException("Application not allowed: " + appName);
        }

        log.info("Opening application: {}", appName);

        try {
            // Map common app names to actual commands
            String command = switch (appName.toLowerCase()) {
                case "code", "vscode" -> "code";
                case "firefox" -> "firefox";
                case "chrome", "google-chrome" -> "google-chrome";
                case "terminal" -> "gnome-terminal";
                case "files", "nautilus" -> "nautilus";
                case "calculator" -> "gnome-calculator";
                case "settings" -> "gnome-control-center";
                default -> appName; // Use as-is for other apps
            };

            // Try different launch methods
            CommandResult result = executeCommand("gtk-launch " + command);
            if (result.isSuccess()) {
                return result;
            }

            // Fallback to direct command
            result = executeCommand(command + " &");
            if (result.isSuccess()) {
                return result;
            }

            // Fallback to which + exec
            result = executeCommand("which " + command + " && " + command + " &");
            return result;
            
        } catch (Exception e) {
            log.error("Failed to open application: {}", appName, e);
            throw new DeviceCommandException("Failed to open application: " + e.getMessage());
        }
    }

    /**
     * Take screenshot
     */
    public CommandResult takeScreenshot() {
        log.info("Taking screenshot");

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "screenshot_" + timestamp + ".png";
            String screenshotPath = securityConfig.getScreenshotPath() + "/" + filename;
            
            // Ensure screenshot directory exists
            Path screenshotDir = Paths.get(securityConfig.getScreenshotPath());
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }

            // Try different screenshot tools
            String[] commands = {
                "gnome-screenshot -f " + screenshotPath,
                "scrot " + screenshotPath,
                "import -window root " + screenshotPath
            };

            for (String command : commands) {
                try {
                    CommandResult result = executeCommand(command);
                    if (result.isSuccess() && Files.exists(Paths.get(screenshotPath))) {
                        result.setOutput("Screenshot saved: " + screenshotPath);
                        return result;
                    }
                } catch (Exception e) {
                    log.debug("Screenshot command failed: {}", command);
                }
            }

            throw new DeviceCommandException("No screenshot tool available");
            
        } catch (Exception e) {
            log.error("Failed to take screenshot", e);
            throw new DeviceCommandException("Failed to take screenshot: " + e.getMessage());
        }
    }

    /**
     * Lock screen
     */
    public CommandResult lockScreen() {
        log.info("Locking screen");

        try {
            // Try different lock commands
            String[] commands = {
                "gnome-screensaver-command -l",
                "loginctl lock-session",
                "dm-tool lock",
                "xdg-screensaver lock"
            };

            for (String command : commands) {
                try {
                    CommandResult result = executeCommand(command);
                    if (result.isSuccess()) {
                        return result;
                    }
                } catch (Exception e) {
                    log.debug("Lock command failed: {}", command);
                }
            }

            throw new DeviceCommandException("No lock command available");
            
        } catch (Exception e) {
            log.error("Failed to lock screen", e);
            throw new DeviceCommandException("Failed to lock screen: " + e.getMessage());
        }
    }

    /**
     * Open URL in default browser
     */
    public CommandResult openUrl(String url) {
        if (!isValidUrl(url)) {
            throw new DeviceCommandException("Invalid URL: " + url);
        }

        log.info("Opening URL: {}", url);

        try {
            String command = "xdg-open '" + url + "'";
            return executeCommand(command);
            
        } catch (Exception e) {
            log.error("Failed to open URL: {}", url, e);
            throw new DeviceCommandException("Failed to open URL: " + e.getMessage());
        }
    }

    /**
     * Execute system command safely
     */
    private CommandResult executeCommand(String command) throws IOException, InterruptedException {
        log.debug("Executing command: {}", command);

        // Security check
        if (!securityConfig.isCommandAllowed(command)) {
            throw new DeviceCommandException("Command not allowed: " + command);
        }

        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // Read output
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        // Wait for completion with timeout
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new DeviceCommandException("Command timeout: " + command);
        }

        int exitCode = process.exitValue();
        String outputStr = output.toString().trim();
        
        log.debug("Command result: exitCode={}, output='{}'", exitCode, outputStr);

        return CommandResult.builder()
                .command(command)
                .exitCode(exitCode)
                .output(outputStr)
                .success(exitCode == 0)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Validate URL
     */
    private boolean isValidUrl(String url) {
        return url != null && 
               (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) &&
               url.length() < 2000; // Reasonable length limit
    }

    /**
     * Get system information
     */
    public CommandResult getSystemInfo() {
        try {
            String command = "uname -a && lsb_release -a 2>/dev/null || cat /etc/os-release";
            return executeCommand(command);
        } catch (Exception e) {
            log.error("Failed to get system info", e);
            throw new DeviceCommandException("Failed to get system info: " + e.getMessage());
        }
    }
}
