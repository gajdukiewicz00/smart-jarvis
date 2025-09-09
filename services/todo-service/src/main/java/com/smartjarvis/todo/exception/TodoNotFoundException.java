package com.smartjarvis.todo.exception;

/**
 * Exception thrown when todo is not found
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(String todoId) {
        super("Todo not found with id: " + todoId);
    }

    public TodoNotFoundException(String todoId, String userId) {
        super("Todo not found with id: " + todoId + " for user: " + userId);
    }
}
