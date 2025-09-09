package com.smartjarvis.gateway.websocket;

import com.smartjarvis.events.AudioIncomingEvent;
import com.smartjarvis.events.WebSocketConnectionEvent;
import com.smartjarvis.events.ConnectionEventType;
import com.smartjarvis.gateway.metrics.VoiceGatewayMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    
    // Active sessions storage
    private final ConcurrentMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    
    // Kafka topics
    private static final String AUDIO_TOPIC = "audio.incoming";
    private static final String CONNECTION_TOPIC = "websocket.connection";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        String userId = extractUserId(session); // TODO: implement authentication
        
        log.info("WebSocket connection established: sessionId={}, userId={}", sessionId, userId);
        
        // Store active session
        activeSessions.put(sessionId, session);
        
        // Update metrics
        metrics.incrementConnections();
        metrics.setActiveConnections(activeSessions.size());
        
        // Publish connection event
        WebSocketConnectionEvent event = WebSocketConnectionEvent.newBuilder()
            .setSessionId(sessionId)
            .setUserId(userId)
            .setEventType(ConnectionEventType.CONNECTED)
            .setTimestamp(Instant.now().toEpochMilli())
            .setClientInfo(session.getHandshakeHeaders().getFirst("User-Agent"))
            .build();
            
        kafkaTemplate.send(CONNECTION_TOPIC, sessionId, event);
        
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
        
        // Update metrics
        metrics.setActiveConnections(activeSessions.size());
        
        // Publish disconnection event
        WebSocketConnectionEvent event = WebSocketConnectionEvent.newBuilder()
            .setSessionId(sessionId)
            .setUserId(userId)
            .setEventType(ConnectionEventType.DISCONNECTED)
            .setTimestamp(Instant.now().toEpochMilli())
            .setClientInfo(status.toString())
            .build();
            
        kafkaTemplate.send(CONNECTION_TOPIC, sessionId, event);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = session.getId();
        String userId = extractUserId(session);
        ByteBuffer payload = message.getPayload();
        
        log.debug("Received audio data: sessionId={}, bytes={}", sessionId, payload.remaining());
        
        // Start timing for metrics
        var timer = metrics.startAudioProcessingTimer();
        
        try {
            // Create audio event
            AudioIncomingEvent event = AudioIncomingEvent.newBuilder()
                .setSessionId(sessionId)
                .setUserId(userId != null ? userId : "anonymous")
                .setAudioData(payload)
                .setTimestamp(Instant.now().toEpochMilli())
                .setFormat("pcm_16khz")
                .setDuration(calculateAudioDuration(payload.remaining()))
                .build();
            
            // Publish to Kafka
            kafkaTemplate.send(AUDIO_TOPIC, sessionId, event);
            
            // Update metrics
            metrics.incrementAudioMessages();
            
            log.debug("Audio event published to Kafka: sessionId={}", sessionId);
            
        } catch (Exception e) {
            log.error("Failed to process audio message: sessionId={}", sessionId, e);
            
            // Send error message back to client
            sendTextMessage(session, "Error processing audio: " + e.getMessage());
            
            // Publish error event
            publishErrorEvent(sessionId, userId, e.getMessage());
            
        } finally {
            timer.stop();
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
            WebSocketConnectionEvent event = WebSocketConnectionEvent.newBuilder()
                .setSessionId(sessionId)
                .setUserId(userId)
                .setEventType(ConnectionEventType.ERROR)
                .setTimestamp(Instant.now().toEpochMilli())
                .setErrorMessage(errorMessage)
                .build();
                
            kafkaTemplate.send(CONNECTION_TOPIC, sessionId, event);
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
}
