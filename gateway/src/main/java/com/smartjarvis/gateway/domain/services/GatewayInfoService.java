package com.smartjarvis.gateway.domain.services;

import com.smartjarvis.gateway.domain.entities.GatewayInfo;

/**
 * Domain service for Gateway information operations
 * 
 * This service encapsulates the business logic for gateway information
 * and follows the Single Responsibility Principle.
 */
public interface GatewayInfoService {
    
    /**
     * Creates gateway information with current timestamp
     * @param name gateway name
     * @param version gateway version
     * @param port gateway port
     * @param securityEnabled security status
     * @return GatewayInfo instance
     */
    GatewayInfo createGatewayInfo(String name, String version, String port, boolean securityEnabled);
    
    /**
     * Validates gateway information
     * @param gatewayInfo the gateway information to validate
     * @return true if valid
     */
    boolean validateGatewayInfo(GatewayInfo gatewayInfo);
    
    /**
     * Checks if gateway is healthy
     * @param gatewayInfo the gateway information to check
     * @return true if healthy
     */
    boolean isGatewayHealthy(GatewayInfo gatewayInfo);
} 