package com.smartjarvis.nlu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result of intent extraction from text
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntentResult {
    
    /**
     * Recognized intent name
     */
    private String intent;
    
    /**
     * Confidence score (0.0 to 1.0)
     */
    private float confidence;
    
    /**
     * Extracted entities
     */
    private Map<String, String> entities;
    
    /**
     * Original text that was processed
     */
    private String originalText;
    
    /**
     * Check if intent recognition was successful
     */
    public boolean isRecognized() {
        return !"unknown".equals(intent) && confidence > 0.5f;
    }
    
    /**
     * Get entity value by key
     */
    public String getEntity(String key) {
        return entities != null ? entities.get(key) : null;
    }
    
    /**
     * Check if entity exists
     */
    public boolean hasEntity(String key) {
        return entities != null && entities.containsKey(key);
    }
}
