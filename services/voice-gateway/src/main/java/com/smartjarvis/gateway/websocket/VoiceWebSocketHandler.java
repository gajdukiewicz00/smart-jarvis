package com.smartjarvis.gateway.websocket;

import com.smartjarvis.gateway.metrics.VoiceGatewayMetrics;
import com.smartjarvis.gateway.service.VoiceActivityDetector;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * WebSocket handler for voice communication
 * Handles audio streaming and publishes events to Kafka
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VoiceWebSocketHandler extends AbstractWebSocketHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final VoiceGatewayMetrics metrics;
    private final VoiceActivityDetector vadService;
    
    // Active sessions storage
    private final ConcurrentMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    // Audio buffering for fragmented messages
    private final ConcurrentMap<String, ByteArrayOutputStream> audioBuffers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> lastAudioTime = new ConcurrentHashMap<>();
    
    // Timer for flushing audio buffers
    private final ScheduledExecutorService audioFlushTimer = Executors.newScheduledThreadPool(1);
    
    // Kafka topics
    private static final String AUDIO_TOPIC = "audio.incoming";
    private static final String CONNECTION_TOPIC = "websocket.connection";
    private static final String BARGEIN_TOPIC = "voice.bargein";
    
    // TTS state tracking for barge-in
    private final ConcurrentMap<String, Boolean> ttsActiveMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String userId = extractUserId(session); // TODO: implement authentication
        
        log.info("WebSocket connection established: sessionId={}, userId={}", sessionId, userId);
        
        // Store active session
        activeSessions.put(sessionId, session);
        
        // Initialize audio buffer for this session
        audioBuffers.put(sessionId, new ByteArrayOutputStream());
        lastAudioTime.put(sessionId, System.currentTimeMillis());
        
        // Update metrics (with null check)
        if (metrics != null) {
            try {
                metrics.incrementConnections();
                metrics.setActiveConnections(activeSessions.size());
            } catch (Exception e) {
                log.warn("Failed to update metrics: {}", e.getMessage());
            }
        }
        
        // Publish connection event (JSON)
        Map<String, Object> event = Map.of(
            "sessionId", sessionId,
            "userId", userId != null ? userId : "anonymous",
            "eventType", "CONNECTED",
            "timestamp", Instant.now().toEpochMilli(),
            "clientInfo", session.getHandshakeHeaders().getFirst("User-Agent") != null ? 
                session.getHandshakeHeaders().getFirst("User-Agent") : "unknown"
        );
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(CONNECTION_TOPIC, sessionId, event);
            } catch (Exception e) {
                log.warn("Failed to send connection event to Kafka: {}", e.getMessage());
            }
        }
        
        // Send welcome message
        sendTextMessage(session, "Connected to SmartJARVIS Voice Gateway");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        String userId = extractUserId(session);
        
        log.info("WebSocket connection closed: sessionId={}, userId={}, status={}", 
                sessionId, userId, status);
        
        // Remove from active sessions
        activeSessions.remove(sessionId);
        
        // Flush any remaining audio data before closing
        flushAudioBuffer(sessionId, userId);
        
        // Clean up audio buffers
        audioBuffers.remove(sessionId);
        lastAudioTime.remove(sessionId);
        
        // Update metrics (with null check)
        if (metrics != null) {
            try {
                metrics.setActiveConnections(activeSessions.size());
            } catch (Exception e) {
                log.warn("Failed to update metrics: {}", e.getMessage());
            }
        }
        
        // Publish disconnection event (JSON)
        Map<String, Object> event = Map.of(
            "sessionId", sessionId,
            "userId", userId != null ? userId : "anonymous",
            "eventType", "DISCONNECTED",
            "timestamp", Instant.now().toEpochMilli(),
            "clientInfo", status != null ? status.toString() : "unknown"
        );
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send(CONNECTION_TOPIC, sessionId, event);
            } catch (Exception e) {
                log.warn("Failed to send disconnection event to Kafka: {}", e.getMessage());
            }
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = session.getId();
        String userId = extractUserId(session);
        ByteBuffer payload = message.getPayload();
        byte[] audioData = payload.array();
        
        log.info("Received audio data: sessionId={}, bytes={}", sessionId, audioData.length);
        
        // Start timing for metrics (with null check)
        Timer.Sample timer = null;
        if (metrics != null) {
            try {
                timer = metrics.startAudioProcessingTimer();
            } catch (Exception e) {
                log.warn("Failed to start audio processing timer: {}", e.getMessage());
            }
        }
        
        try {
            // Check for barge-in before processing audio
            boolean isTtsActive = ttsActiveMap.getOrDefault(sessionId, false);
            
            if (vadService != null && vadService.shouldTriggerBargeIn(sessionId, audioData, isTtsActive)) {
                // Trigger barge-in
                publishBargeInEvent(sessionId, userId);
                ttsActiveMap.put(sessionId, false); // Mark TTS as stopped
                
                // Send immediate feedback to client
                sendTextMessage(session, "{\"type\":\"barge_in\",\"message\":\"TTS interrupted\"}");
            }
            
            // Buffer audio data for fragmented messages
            audioBuffers.computeIfAbsent(sessionId, k -> new ByteArrayOutputStream()).write(audioData);
            lastAudioTime.put(sessionId, System.currentTimeMillis());
            
            // Check if we should flush the buffer (after 500ms of silence or buffer size > 64KB)
            ByteArrayOutputStream buffer = audioBuffers.get(sessionId);
            if (buffer.size() > 65536) { // 64KB threshold
                flushAudioBuffer(sessionId, userId);
            }
            
            // Update metrics (with null check)
            if (metrics != null) {
                try {
                    metrics.incrementAudioMessages();
                } catch (Exception e) {
                    log.warn("Failed to increment audio messages counter: {}", e.getMessage());
                }
            }
            
            log.debug("Audio event published to Kafka: sessionId={}", sessionId);
            
        } catch (Exception e) {
            log.error("Failed to process audio message: sessionId={}", sessionId, e);
            
            // Send error message back to client
            sendTextMessage(session, "{\"type\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            
            // Publish error event
            publishErrorEvent(sessionId, userId, e.getMessage());
            
        } finally {
            if (metrics != null && timer != null) {
                try {
                    metrics.stopAudioProcessingTimer(timer);
                } catch (Exception e) {
                    log.warn("Failed to stop audio processing timer: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload();
        
        log.debug("Received text message: sessionId={}, message={}", sessionId, payload);
        
        // Handle control messages
        switch (payload.toLowerCase()) {
            case "ping":
                sendTextMessage(session, "pong");
                break;
            case "status":
                sendTextMessage(session, "Connected to SmartJARVIS Voice Gateway");
                break;
            default:
                sendTextMessage(session, "Unknown command: " + payload);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        String userId = extractUserId(session);
        
        log.error("WebSocket transport error: sessionId={}", sessionId, exception);
        
        // Publish error event
        publishErrorEvent(sessionId, userId, exception.getMessage());
        
        // Close session if still open
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private String extractUserId(WebSocketSession session) {
        // TODO: Implement proper authentication
        // For now, return null (anonymous user)
        return session.getHandshakeHeaders().getFirst("X-User-Id");
    }

    private void sendTextMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.error("Failed to send text message: sessionId={}", session.getId(), e);
        }
    }

    private Double calculateAudioDuration(int audioBytes) {
        // Assuming PCM 16kHz, 16-bit, mono
        // 16000 samples/sec * 2 bytes/sample = 32000 bytes/sec
        return audioBytes / 32000.0;
    }

    private void publishErrorEvent(String sessionId, String userId, String errorMessage) {
        try {
            Map<String, Object> event = Map.of(
                "sessionId", sessionId,
                "userId", userId != null ? userId : "anonymous",
                "eventType", "ERROR",
                "timestamp", Instant.now().toEpochMilli(),
                "errorMessage", errorMessage != null ? errorMessage : "Unknown error"
            );
            if (kafkaTemplate != null) {
                kafkaTemplate.send(CONNECTION_TOPIC, sessionId, event);
            }
        } catch (Exception e) {
            log.error("Failed to publish error event: sessionId={}", sessionId, e);
        }
    }

    // Public methods for external access
    public int getActiveConnectionsCount() {
        return activeSessions.size();
    }

    public boolean isSessionActive(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }
    
    /**
     * Publish barge-in event
     */
    private void publishBargeInEvent(String sessionId, String userId) {
        try {
            // Create simplified barge-in event
            Map<String, Object> bargeInEvent = Map.of(
                "sessionId", sessionId,
                "userId", userId != null ? userId : "anonymous",
                "timestamp", Instant.now().toEpochMilli(),
                "reason", "voice_detected"
            );
            
            if (kafkaTemplate != null) {
                kafkaTemplate.send(BARGEIN_TOPIC, sessionId, bargeInEvent);
            }
            
            log.info("Barge-in event published: sessionId={}", sessionId);
            
        } catch (Exception e) {
            log.error("Failed to publish barge-in event: sessionId={}", sessionId, e);
        }
    }
    
    /**
     * Set TTS active state for session
     */
    public void setTtsActive(String sessionId, boolean active) {
        if (active) {
            ttsActiveMap.put(sessionId, true);
        } else {
            ttsActiveMap.remove(sessionId);
        }
        log.debug("TTS state updated: sessionId={}, active={}", sessionId, active);
    }
    
    /**
     * Check if TTS is active for session
     */
    public boolean isTtsActive(String sessionId) {
        return ttsActiveMap.getOrDefault(sessionId, false);
    }
    
    /**
     * Handle TTS start notification
     */
    public void handleTtsStart(String sessionId) {
        setTtsActive(sessionId, true);
        
        // Send TTS start notification to client
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null && session.isOpen()) {
            sendTextMessage(session, "{\"type\":\"tts_start\",\"message\":\"Speaking...\"}");
        }
    }
    
    /**
     * Handle TTS stop notification
     */
    public void handleTtsStop(String sessionId) {
        setTtsActive(sessionId, false);
        
        // Send TTS stop notification to client
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null && session.isOpen()) {
            sendTextMessage(session, "{\"type\":\"tts_stop\",\"message\":\"Finished speaking\"}");
        }
    }
    
    /**
     * Flush audio buffer and send to Kafka
     */
    private void flushAudioBuffer(String sessionId, String userId) {
        ByteArrayOutputStream buffer = audioBuffers.get(sessionId);
        if (buffer != null && buffer.size() > 0) {
            byte[] audioData = buffer.toByteArray();
            
            log.info("Flushing audio buffer: sessionId={}, bytes={}", sessionId, audioData.length);
            
            // Create audio event
            Map<String, Object> audioEvent = Map.of(
                "sessionId", sessionId,
                "userId", userId != null ? userId : "anonymous",
                "audioData", audioData,
                "timestamp", Instant.now().toEpochMilli(),
                "format", "webm_opus",
                "duration", calculateAudioDuration(audioData.length)
            );
            
            // Send to Kafka
            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send(AUDIO_TOPIC, sessionId, audioEvent);
                    log.info("Audio buffer sent to Kafka: sessionId={}, bytes={}", sessionId, audioData.length);
                } catch (Exception e) {
                    log.warn("Failed to send audio buffer to Kafka: {}", e.getMessage());
                }
            }
            
            // Clear buffer
            buffer.reset();
        }
    }
}
