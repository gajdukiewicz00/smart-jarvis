package com.smartjarvis.dm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response action configuration for intents
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseAction {
    
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
     * Whether action requires user confirmation
     */
    @Builder.Default
    private boolean requiresConfirmation = false;
    
    /**
     * Action priority (1=low, 5=high)
     */
    @Builder.Default
    private int priority = 1;
}
