package com.smartjarvis.gateway.infrastructure.services;

import com.smartjarvis.gateway.domain.entities.GatewayInfo;
import com.smartjarvis.gateway.domain.services.GatewayInfoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of GatewayInfoService in the infrastructure layer
 * 
 * This implementation provides the concrete business logic for gateway information
 * and follows the Single Responsibility Principle.
 */
@Service
public class GatewayInfoServiceImpl implements GatewayInfoService {
    
    @Override
    public GatewayInfo createGatewayInfo(String name, String version, String port, boolean securityEnabled) {
        Map<String, String> availableServices = new HashMap<>();
        availableServices.put("task-service", "/api/v1/tasks/**");
        availableServices.put("nlp-engine", "/api/v1/nlp/**");
        availableServices.put("speech-service", "/api/v1/speech/**");
        
        return GatewayInfo.builder()
                .name(name)
                .version(version)
                .port(port)
                .timestamp(LocalDateTime.now())
                .securityEnabled(securityEnabled)
                .status("UP")
                .availableServices(availableServices)
                .build();
    }
    
    @Override
    public boolean validateGatewayInfo(GatewayInfo gatewayInfo) {
        return gatewayInfo != null && gatewayInfo.isValid();
    }
    
    @Override
    public boolean isGatewayHealthy(GatewayInfo gatewayInfo) {
        return gatewayInfo != null && gatewayInfo.isOperational();
    }
} 