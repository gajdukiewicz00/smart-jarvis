package com.smartjarvis.device.exception;

/**
 * Exception thrown when device command fails
 */
public class DeviceCommandException extends RuntimeException {

    public DeviceCommandException(String message) {
        super(message);
    }

    public DeviceCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
