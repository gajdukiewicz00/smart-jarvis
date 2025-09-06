package com.smartjarvis.gateway.domain.entities;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Domain entity representing Gateway information
 * 
 * This entity encapsulates the core business logic for gateway information
 * and follows the Single Responsibility Principle.
 */
public class GatewayInfo {
    
    private final String name;
    private final String version;
    private final String port;
    private final LocalDateTime timestamp;
    private final boolean securityEnabled;
    private final String status;
    private final Map<String, String> availableServices;
    
    private GatewayInfo(Builder builder) {
        this.name = builder.name;
        this.version = builder.version;
        this.port = builder.port;
        this.timestamp = builder.timestamp;
        this.securityEnabled = builder.securityEnabled;
        this.status = builder.status;
        this.availableServices = builder.availableServices;
    }
    
    // Getters
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getPort() { return port; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSecurityEnabled() { return securityEnabled; }
    public String getStatus() { return status; }
    public Map<String, String> getAvailableServices() { return availableServices; }
    
    /**
     * Validates the gateway information
     * @return true if the information is valid
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               version != null && !version.trim().isEmpty() &&
               port != null && !port.trim().isEmpty() &&
               status != null && !status.trim().isEmpty();
    }
    
    /**
     * Checks if the gateway is operational
     * @return true if the gateway is operational
     */
    public boolean isOperational() {
        return "UP".equalsIgnoreCase(status);
    }
    
    /**
     * Builder pattern for GatewayInfo
     */
    public static class Builder {
        private String name;
        private String version;
        private String port;
        private LocalDateTime timestamp;
        private boolean securityEnabled;
        private String status;
        private Map<String, String> availableServices;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder port(String port) {
            this.port = port;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder securityEnabled(boolean securityEnabled) {
            this.securityEnabled = securityEnabled;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder availableServices(Map<String, String> availableServices) {
            this.availableServices = availableServices;
            return this;
        }
        
        public GatewayInfo build() {
            return new GatewayInfo(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
} 