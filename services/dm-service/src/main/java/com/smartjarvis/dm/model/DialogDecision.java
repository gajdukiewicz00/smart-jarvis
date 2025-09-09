package com.smartjarvis.dm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result of dialog management decision
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DialogDecision {
    
    /**
     * Session identifier
     */
    private String sessionId;
    
    /**
     * User identifier
     */
    private String userId;
    
    /**
     * Action to be performed
     */
    private String action;
    
    /**
     * Target service to handle the action
     */
    private String targetService;
    
    /**
     * Response text to be spoken
     */
    private String responseText;
    
    /**
     * Action parameters
     */
    private Map<String, String> parameters;
    
    /**
     * Whether action requires user confirmation
     */
    private boolean requiresConfirmation;
    
    /**
     * Decision timestamp
     */
    private long timestamp;
    
    /**
     * Check if decision is actionable
     */
    public boolean isActionable() {
        return action != null && !action.trim().isEmpty() && 
               targetService != null && !targetService.trim().isEmpty();
    }
    
    /**
     * Check if decision is just a response
     */
    public boolean isResponseOnly() {
        return "respond".equals(action) && "tts-service".equals(targetService);
    }
    
    /**
     * Get parameter value by key
     */
    public String getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }
}
