package com.smartjarvis.home.client;

import com.smartjarvis.home.config.HomeAssistantConfig;
import com.smartjarvis.home.exception.HomeControlException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * HTTP client for Home Assistant REST API
 */
@Component
@Slf4j
public class HomeAssistantClient {

    private final HomeAssistantConfig config;
    private final WebClient webClient;

    public HomeAssistantClient(HomeAssistantConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getToken())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Call Home Assistant service
     */
    public void callService(String domain, String service, String entityId, Map<String, Object> data) {
        try {
            log.debug("Calling HA service: {}.{} for entity {}", domain, service, entityId);

            Map<String, Object> requestBody = Map.of(
                "entity_id", entityId
            );

            // Add data if provided
            if (data != null && !data.isEmpty()) {
                requestBody = Map.of(
                    "entity_id", entityId,
                    "data", data
                );
            }

            String response = webClient.post()
                    .uri("/api/services/{domain}/{service}", domain, service)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                    new HomeControlException("HA API error: " + errorBody)));
                    })
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.debug("HA service call successful: {}.{}", domain, service);

        } catch (Exception e) {
            log.error("Failed to call HA service: {}.{} for entity {}", domain, service, entityId, e);
            throw new HomeControlException("Failed to call HA service: " + e.getMessage());
        }
    }

    /**
     * Get entity state
     */
    public Map<String, Object> getEntityState(String entityId) {
        try {
            log.debug("Getting entity state: {}", entityId);

            Map<String, Object> state = webClient.get()
                    .uri("/api/states/{entity_id}", entityId)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        return Mono.error(new HomeControlException("Entity not found: " + entityId));
                    })
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            log.debug("Entity state retrieved: {}", entityId);
            return state;

        } catch (Exception e) {
            log.error("Failed to get entity state: {}", entityId, e);
            throw new HomeControlException("Failed to get entity state: " + e.getMessage());
        }
    }

    /**
     * Get all states
     */
    public Map<String, Object> getStates() {
        try {
            log.debug("Getting all HA states");

            Map<String, Object> states = webClient.get()
                    .uri("/api/states")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.debug("All states retrieved");
            return states;

        } catch (Exception e) {
            log.error("Failed to get HA states", e);
            throw new HomeControlException("Failed to get HA states: " + e.getMessage());
        }
    }

    /**
     * Check connection to Home Assistant
     */
    public boolean checkConnection() {
        try {
            log.debug("Checking HA connection");

            String response = webClient.get()
                    .uri("/api/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            boolean isConnected = response != null && response.contains("message");
            log.debug("HA connection check: {}", isConnected ? "SUCCESS" : "FAILED");
            
            return isConnected;

        } catch (Exception e) {
            log.warn("HA connection check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test service call (for health checks)
     */
    public boolean testServiceCall() {
        try {
            // Call a harmless service to test connectivity
            webClient.get()
                    .uri("/api/config")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            
            return true;
            
        } catch (Exception e) {
            log.debug("HA test service call failed: {}", e.getMessage());
            return false;
        }
    }
}
