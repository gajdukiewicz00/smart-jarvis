package com.smartjarvis.home.exception;

/**
 * Exception thrown when home control operation fails
 */
public class HomeControlException extends RuntimeException {

    public HomeControlException(String message) {
        super(message);
    }

    public HomeControlException(String message, Throwable cause) {
        super(message, cause);
    }
}
