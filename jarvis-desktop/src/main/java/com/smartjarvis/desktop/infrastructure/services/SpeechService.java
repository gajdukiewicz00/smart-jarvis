package com.smartjarvis.desktop.infrastructure.services;

/**
 * Service interface for speech processing
 */
public interface SpeechService {
    
    /**
     * Convert speech to text
     * @param audioData the audio data to process
     * @return transcribed text
     */
    String speechToText(String audioData);
    
    /**
     * Convert text to speech
     * @param text the text to convert to speech
     */
    void textToSpeech(String text);
    
    /**
     * Listen for voice input
     * @return transcribed text from microphone
     */
    String listenForSpeech();
} 