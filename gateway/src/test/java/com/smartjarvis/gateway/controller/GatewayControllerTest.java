package com.smartjarvis.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.WebTestClient;

/**
 * Tests for Gateway Controller
 */
@WebFluxTest(GatewayController.class)
class GatewayControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetGatewayInfo() {
        webTestClient.get()
                .uri("/api/v1/gateway/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("gateway-service")
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void testGetGatewayHealth() {
        webTestClient.get()
                .uri("/api/v1/gateway/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("gateway-service");
    }

    @Test
    void testGetApiDocs() {
        webTestClient.get()
                .uri("/api/v1/gateway/docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("SmartJARVIS API Gateway")
                .jsonPath("$.version").isEqualTo("1.0.0");
    }
}