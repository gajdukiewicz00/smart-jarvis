package com.smartjarvis.gateway.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metrics collection for Voice Gateway service
 */
@Component
@Slf4j
public class VoiceGatewayMetrics {

    private final Counter connectionsTotal;
    private final AtomicInteger activeConnections;
    private final Counter audioMessagesTotal;
    private final Timer audioProcessingTime;
    private final Counter errorsTotal;

    public VoiceGatewayMetrics(MeterRegistry meterRegistry) {
        try {
            // Connection metrics
            this.connectionsTotal = Counter.builder("voice_gateway_connections_total")
                .description("Total number of WebSocket connections established")
                .tag("service", "voice-gateway")
                .register(meterRegistry);

            this.activeConnections = new AtomicInteger(0);
            Gauge.builder("voice_gateway_active_connections", activeConnections, AtomicInteger::get)
                .description("Current number of active WebSocket connections")
                .tag("service", "voice-gateway")
                .register(meterRegistry);

            // Audio processing metrics
            this.audioMessagesTotal = Counter.builder("voice_gateway_audio_messages_total")
                .description("Total number of audio messages processed")
                .tag("service", "voice-gateway")
                .register(meterRegistry);

            this.audioProcessingTime = Timer.builder("voice_gateway_audio_processing_seconds")
                .description("Time spent processing audio messages")
                .tag("service", "voice-gateway")
                .register(meterRegistry);

            // Error metrics
            this.errorsTotal = Counter.builder("voice_gateway_errors_total")
                .description("Total number of errors occurred")
                .tag("service", "voice-gateway")
                .register(meterRegistry);

            log.info("Voice Gateway metrics initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Voice Gateway metrics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize metrics", e);
        }
    }

    public void incrementConnections() {
        connectionsTotal.increment();
        log.debug("Connection counter incremented: {}", connectionsTotal.count());
    }

    public void setActiveConnections(int count) {
        activeConnections.set(count);
        log.debug("Active connections updated: {}", count);
    }

    public void incrementAudioMessages() {
        audioMessagesTotal.increment();
        log.debug("Audio messages counter incremented: {}", audioMessagesTotal.count());
    }

    public Timer.Sample startAudioProcessingTimer() {
        return Timer.start();
    }

    public void stopAudioProcessingTimer(Timer.Sample sample) {
        sample.stop(audioProcessingTime);
    }

    public void incrementErrors() {
        errorsTotal.increment();
        log.debug("Errors counter incremented: {}", errorsTotal.count());
    }

    private double getActiveConnectionsValue() {
        return activeConnections.get();
    }

    // Getter methods for external access
    public double getConnectionsTotal() {
        return connectionsTotal.count();
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

    public double getAudioMessagesTotal() {
        return audioMessagesTotal.count();
    }

    public double getErrorsTotal() {
        return errorsTotal.count();
    }
}
