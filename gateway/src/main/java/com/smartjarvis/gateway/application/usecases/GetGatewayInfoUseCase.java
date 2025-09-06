package com.smartjarvis.gateway.application.usecases;

import com.smartjarvis.gateway.application.dto.GatewayInfoDto;
import com.smartjarvis.gateway.domain.entities.GatewayInfo;
import com.smartjarvis.gateway.domain.services.GatewayInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Use Case for getting Gateway information
 * 
 * This use case orchestrates the business logic for retrieving gateway information
 * and follows the Single Responsibility Principle.
 */
@Service
public class GetGatewayInfoUseCase {
    
    private final GatewayInfoService gatewayInfoService;
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${server.port}")
    private String serverPort;
    
    @Value("${gateway.security.enabled:false}")
    private boolean securityEnabled;
    
    public GetGatewayInfoUseCase(GatewayInfoService gatewayInfoService) {
        this.gatewayInfoService = gatewayInfoService;
    }
    
    /**
     * Executes the use case to get gateway information
     * @return GatewayInfoDto with current gateway information
     */
    public GatewayInfoDto execute() {
        // Create available services map
        Map<String, String> availableServices = createAvailableServicesMap();
        
        // Create domain entity
        GatewayInfo gatewayInfo = gatewayInfoService.createGatewayInfo(
            applicationName,
            "1.0.0",
            serverPort,
            securityEnabled
        );
        
        // Validate domain entity
        if (!gatewayInfoService.validateGatewayInfo(gatewayInfo)) {
            throw new IllegalStateException("Invalid gateway information");
        }
        
        // Convert to DTO
        return convertToDto(gatewayInfo, availableServices);
    }
    
    /**
     * Creates the map of available services
     * @return Map of service names to their endpoints
     */
    private Map<String, String> createAvailableServicesMap() {
        Map<String, String> services = new HashMap<>();
        services.put("task-service", "/api/v1/tasks/**");
        services.put("nlp-engine", "/api/v1/nlp/**");
        services.put("speech-service", "/api/v1/speech/**");
        return services;
    }
    
    /**
     * Converts domain entity to DTO
     * @param gatewayInfo the domain entity
     * @param availableServices the available services map
     * @return GatewayInfoDto
     */
    private GatewayInfoDto convertToDto(GatewayInfo gatewayInfo, Map<String, String> availableServices) {
        return new GatewayInfoDto(
            gatewayInfo.getName(),
            gatewayInfo.getVersion(),
            gatewayInfo.getPort(),
            gatewayInfo.getTimestamp(),
            gatewayInfo.isSecurityEnabled(),
            gatewayInfo.getStatus(),
            availableServices
        );
    }
} 