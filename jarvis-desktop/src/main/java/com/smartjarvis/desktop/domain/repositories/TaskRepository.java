package com.smartjarvis.desktop.domain.repositories;

import com.smartjarvis.desktop.domain.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Task domain entity
 */
public interface TaskRepository {
    
    /**
     * Save a task
     * @param task the task to save
     * @return the saved task
     */
    Task save(Task task);
    
    /**
     * Find a task by its ID
     * @param id the task ID
     * @return Optional containing the task if found
     */
    Optional<Task> findById(UUID id);
    
    /**
     * Find all tasks
     * @return list of all tasks
     */
    List<Task> findAll();
    
    /**
     * Find tasks by status
     * @param status the task status
     * @return list of tasks with the specified status
     */
    List<Task> findByStatus(Task.TaskStatus status);
    
    /**
     * Find tasks by priority
     * @param priority the task priority
     * @return list of tasks with the specified priority
     */
    List<Task> findByPriority(Task.TaskPriority priority);
    
    /**
     * Find overdue tasks
     * @return list of overdue tasks
     */
    List<Task> findOverdueTasks();
    
    /**
     * Find tasks due soon (within next 24 hours)
     * @return list of tasks due soon
     */
    List<Task> findTasksDueSoon();
    
    /**
     * Find tasks by category
     * @param category the task category
     * @return list of tasks in the specified category
     */
    List<Task> findByCategory(String category);
    
    /**
     * Delete a task by its ID
     * @param id the task ID to delete
     */
    void deleteById(UUID id);
    
    /**
     * Check if a task exists by ID
     * @param id the task ID
     * @return true if task exists, false otherwise
     */
    boolean existsById(UUID id);
    
    /**
     * Count total number of tasks
     * @return total task count
     */
    long count();
    
    /**
     * Count tasks by status
     * @param status the task status
     * @return count of tasks with the specified status
     */
    long countByStatus(Task.TaskStatus status);
} 