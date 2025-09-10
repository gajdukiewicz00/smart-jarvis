package com.smartjarvis.home.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result of home control command execution
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeCommandResult {

    /**
     * Whether command was successful
     */
    private boolean success;

    /**
     * Action that was performed
     */
    private String action;

    /**
     * Target entity ID
     */
    private String entityId;

    /**
     * Success message
     */
    private String message;

    /**
     * Error message if failed
     */
    private String errorMessage;

    /**
     * Execution timestamp
     */
    private long timestamp;

    /**
     * Additional data from HA response
     */
    private Map<String, Object> responseData;
}
