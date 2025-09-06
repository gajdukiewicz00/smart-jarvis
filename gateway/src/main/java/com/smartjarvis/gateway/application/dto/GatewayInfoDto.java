package com.smartjarvis.gateway.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for Gateway information
 * 
 * This DTO is used for transferring gateway information between layers
 * and follows the Single Responsibility Principle.
 */
public class GatewayInfoDto {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("port")
    private String port;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("securityEnabled")
    private boolean securityEnabled;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("availableServices")
    private Map<String, String> availableServices;
    
    // Default constructor for JSON deserialization
    public GatewayInfoDto() {}
    
    // Constructor with all fields
    public GatewayInfoDto(String name, String version, String port, LocalDateTime timestamp, 
                         boolean securityEnabled, String status, Map<String, String> availableServices) {
        this.name = name;
        this.version = version;
        this.port = port;
        this.timestamp = timestamp;
        this.securityEnabled = securityEnabled;
        this.status = status;
        this.availableServices = availableServices;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean isSecurityEnabled() { return securityEnabled; }
    public void setSecurityEnabled(boolean securityEnabled) { this.securityEnabled = securityEnabled; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Map<String, String> getAvailableServices() { return availableServices; }
    public void setAvailableServices(Map<String, String> availableServices) { 
        this.availableServices = availableServices; 
    }
    
    /**
     * Validates the DTO
     * @return true if the DTO is valid
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               version != null && !version.trim().isEmpty() &&
               port != null && !port.trim().isEmpty() &&
               status != null && !status.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "GatewayInfoDto{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", port='" + port + '\'' +
                ", timestamp=" + timestamp +
                ", securityEnabled=" + securityEnabled +
                ", status='" + status + '\'' +
                ", availableServices=" + availableServices +
                '}';
    }
} 