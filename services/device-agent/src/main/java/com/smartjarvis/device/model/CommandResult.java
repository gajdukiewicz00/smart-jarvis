package com.smartjarvis.device.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of device command execution
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandResult {

    /**
     * Executed command
     */
    private String command;

    /**
     * Exit code
     */
    private int exitCode;

    /**
     * Command output
     */
    private String output;

    /**
     * Whether command was successful
     */
    private boolean success;

    /**
     * Execution timestamp
     */
    private long timestamp;

    /**
     * Error message if any
     */
    private String errorMessage;

    /**
     * Execution duration in milliseconds
     */
    private long durationMs;
}
