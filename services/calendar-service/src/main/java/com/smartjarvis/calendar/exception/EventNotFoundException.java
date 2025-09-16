package com.smartjarvis.calendar.exception;

/**
 * Exception thrown when calendar event is not found
 */
public class EventNotFoundException extends RuntimeException {
    
    public EventNotFoundException(String message) {
        super(message);
    }
    
    public EventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static EventNotFoundException byId(String eventId) {
        return new EventNotFoundException("Event not found with id: " + eventId);
    }
    
    public static EventNotFoundException byUserIdAndTitle(String userId, String title) {
        return new EventNotFoundException("Event not found for user: " + userId + " with title: " + title);
    }
}
