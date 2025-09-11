package com.smartjarvis.gateway.config;

import com.smartjarvis.gateway.websocket.VoiceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for voice communication
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoiceWebSocketHandler voiceWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Native WebSocket endpoint (used by web UI via Nginx proxy at /voice)
        registry.addHandler(voiceWebSocketHandler, "/voice")
                .setAllowedOrigins("*");

        // Optional SockJS fallback endpoint
        registry.addHandler(voiceWebSocketHandler, "/voice-sockjs")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
