package com.smartjarvis.gateway.domain.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GatewayInfo domain entity
 */
class GatewayInfoTest {

    private GatewayInfo gatewayInfo;
    private Map<String, String> availableServices;

    @BeforeEach
    void setUp() {
        availableServices = new HashMap<>();
        availableServices.put("task-service", "/api/v1/tasks/**");
        availableServices.put("nlp-engine", "/api/v1/nlp/**");
        availableServices.put("speech-service", "/api/v1/speech/**");

        gatewayInfo = GatewayInfo.builder()
                .name("test-gateway")
                .version("1.0.0")
                .port("8080")
                .timestamp(LocalDateTime.now())
                .securityEnabled(false)
                .status("UP")
                .availableServices(availableServices)
                .build();
    }

    @Test
    void testGatewayInfoCreation() {
        assertNotNull(gatewayInfo);
        assertEquals("test-gateway", gatewayInfo.getName());
        assertEquals("1.0.0", gatewayInfo.getVersion());
        assertEquals("8080", gatewayInfo.getPort());
        assertFalse(gatewayInfo.isSecurityEnabled());
        assertEquals("UP", gatewayInfo.getStatus());
        assertEquals(availableServices, gatewayInfo.getAvailableServices());
    }

    @Test
    void testIsValid_WithValidData_ReturnsTrue() {
        assertTrue(gatewayInfo.isValid());
    }

    @Test
    void testIsValid_WithNullName_ReturnsFalse() {
        GatewayInfo invalidGateway = GatewayInfo.builder()
                .name(null)
                .version("1.0.0")
                .port("8080")
                .status("UP")
                .build();
        assertFalse(invalidGateway.isValid());
    }

    @Test
    void testIsValid_WithEmptyName_ReturnsFalse() {
        GatewayInfo invalidGateway = GatewayInfo.builder()
                .name("")
                .version("1.0.0")
                .port("8080")
                .status("UP")
                .build();
        assertFalse(invalidGateway.isValid());
    }

    @Test
    void testIsOperational_WithUpStatus_ReturnsTrue() {
        assertTrue(gatewayInfo.isOperational());
    }

    @Test
    void testIsOperational_WithDownStatus_ReturnsFalse() {
        GatewayInfo downGateway = GatewayInfo.builder()
                .name("test-gateway")
                .version("1.0.0")
                .port("8080")
                .status("DOWN")
                .build();
        assertFalse(downGateway.isOperational());
    }

    @Test
    void testBuilderPattern() {
        GatewayInfo builtGateway = GatewayInfo.builder()
                .name("built-gateway")
                .version("2.0.0")
                .port("9090")
                .timestamp(LocalDateTime.now())
                .securityEnabled(true)
                .status("UP")
                .availableServices(availableServices)
                .build();

        assertNotNull(builtGateway);
        assertEquals("built-gateway", builtGateway.getName());
        assertEquals("2.0.0", builtGateway.getVersion());
        assertEquals("9090", builtGateway.getPort());
        assertTrue(builtGateway.isSecurityEnabled());
    }
} 