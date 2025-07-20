package com.smartjarvis.desktop.infrastructure.services;

import com.smartjarvis.desktop.domain.Task;
import java.util.UUID;

/**
 * Client interface for Task Service communication
 */
public interface TaskServiceClient {
    
    /**
     * Create a task on the remote service
     * @param task the task to create
     * @return the created task
     */
    Task createTask(Task task);
    
    /**
     * Update a task on the remote service
     * @param task the task to update
     * @return the updated task
     */
    Task updateTask(Task task);
    
    /**
     * Delete a task on the remote service
     * @param taskId the task ID to delete
     */
    void deleteTask(UUID taskId);
    
    /**
     * Get a task by ID from the remote service
     * @param taskId the task ID
     * @return the task if found
     */
    Task getTask(UUID taskId);
    
    /**
     * Get all tasks from the remote service
     * @return list of all tasks
     */
    java.util.List<Task> getAllTasks();
} 