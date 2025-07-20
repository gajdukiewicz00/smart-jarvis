package com.smartjarvis.desktop.infrastructure.services;

import com.smartjarvis.desktop.domain.Command;

/**
 * Service interface for NLP processing
 */
public interface NLPService {
    
    /**
     * Process text input and extract intent
     * @param text the text to process
     * @return Command object with extracted intent
     */
    Command processIntent(String text);
    
    /**
     * Process text input with context
     * @param text the text to process
     * @param context additional context information
     * @return Command object with extracted intent
     */
    Command processIntent(String text, Object context);
} 