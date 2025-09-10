package com.smartjarvis.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Voice Activity Detection service
 * Detects voice activity in audio stream for barge-in functionality
 */
@Component
@Slf4j
public class VoiceActivityDetector {

    // VAD configuration
    private static final double SILENCE_THRESHOLD = 0.01;
    private static final double VOICE_THRESHOLD = 0.03;
    private static final int MIN_VOICE_DURATION_MS = 100;
    private static final int MIN_SILENCE_DURATION_MS = 500;
    
    // State tracking per session
    private final java.util.Map<String, VadState> sessionStates = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Analyze audio chunk for voice activity
     */
    public VadResult analyzeAudio(String sessionId, byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return VadResult.SILENCE;
        }

        try {
            // Calculate audio energy
            double energy = calculateAudioEnergy(audioData);
            
            // Get or create session state
            VadState state = sessionStates.computeIfAbsent(sessionId, k -> new VadState());
            
            // Determine current activity
            boolean isVoiceActive = energy > VOICE_THRESHOLD;
            boolean isSilence = energy < SILENCE_THRESHOLD;
            
            long currentTime = System.currentTimeMillis();
            
            // Update state based on current activity
            if (isVoiceActive) {
                if (state.lastVoiceTime == 0) {
                    state.lastVoiceTime = currentTime;
                }
                state.lastSilenceTime = 0;
                
                // Check if voice duration is sufficient
                if (currentTime - state.lastVoiceTime >= MIN_VOICE_DURATION_MS) {
                    state.isVoiceDetected = true;
                    log.debug("Voice activity detected: sessionId={}, energy={:.4f}", sessionId, energy);
                    return VadResult.VOICE_DETECTED;
                }
            } else if (isSilence) {
                if (state.lastSilenceTime == 0) {
                    state.lastSilenceTime = currentTime;
                }
                
                // Check if silence duration is sufficient to end voice activity
                if (state.isVoiceDetected && 
                    currentTime - state.lastSilenceTime >= MIN_SILENCE_DURATION_MS) {
                    state.isVoiceDetected = false;
                    state.lastVoiceTime = 0;
                    log.debug("Voice activity ended: sessionId={}", sessionId);
                    return VadResult.VOICE_ENDED;
                }
                
                if (!state.isVoiceDetected) {
                    return VadResult.SILENCE;
                }
            }
            
            // Transitional state
            return state.isVoiceDetected ? VadResult.VOICE_ACTIVE : VadResult.SILENCE;
            
        } catch (Exception e) {
            log.error("VAD analysis failed for session: {}", sessionId, e);
            return VadResult.SILENCE;
        }
    }

    /**
     * Calculate audio energy (RMS)
     */
    private double calculateAudioEnergy(byte[] audioData) {
        if (audioData.length < 2) {
            return 0.0;
        }

        ByteBuffer buffer = ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN);
        
        double sum = 0.0;
        int sampleCount = 0;
        
        // Process as 16-bit PCM samples
        while (buffer.remaining() >= 2) {
            short sample = buffer.getShort();
            double normalizedSample = sample / 32768.0; // Normalize to [-1, 1]
            sum += normalizedSample * normalizedSample;
            sampleCount++;
        }
        
        if (sampleCount == 0) {
            return 0.0;
        }
        
        // Return RMS (Root Mean Square)
        return Math.sqrt(sum / sampleCount);
    }

    /**
     * Check if barge-in should be triggered
     */
    public boolean shouldTriggerBargeIn(String sessionId, byte[] audioData, boolean isTtsActive) {
        if (!isTtsActive) {
            return false; // No TTS to interrupt
        }

        VadResult result = analyzeAudio(sessionId, audioData);
        boolean shouldTrigger = result == VadResult.VOICE_DETECTED;
        
        if (shouldTrigger) {
            log.info("Barge-in triggered: sessionId={}", sessionId);
        }
        
        return shouldTrigger;
    }

    /**
     * Clear session state
     */
    public void clearSession(String sessionId) {
        sessionStates.remove(sessionId);
        log.debug("VAD session cleared: {}", sessionId);
    }

    /**
     * Get VAD statistics for session
     */
    public VadStats getSessionStats(String sessionId) {
        VadState state = sessionStates.get(sessionId);
        if (state == null) {
            return new VadStats(false, 0, 0);
        }
        
        return new VadStats(
            state.isVoiceDetected,
            state.lastVoiceTime,
            state.lastSilenceTime
        );
    }

    /**
     * VAD state per session
     */
    private static class VadState {
        boolean isVoiceDetected = false;
        long lastVoiceTime = 0;
        long lastSilenceTime = 0;
    }

    /**
     * VAD analysis result
     */
    public enum VadResult {
        SILENCE,
        VOICE_DETECTED,
        VOICE_ACTIVE,
        VOICE_ENDED
    }

    /**
     * VAD statistics record
     */
    public record VadStats(boolean isVoiceActive, long lastVoiceTime, long lastSilenceTime) {}
}
