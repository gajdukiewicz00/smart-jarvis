package com.smartjarvis.taskservice.domain.repositories;

import com.smartjarvis.taskservice.domain.entities.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Task domain entity
 * 
 * This interface defines the contract for data access operations
 * and follows the Dependency Inversion Principle.
 */
public interface TaskRepository {
    
    /**
     * Save a task
     * @param task the task to save
     * @return the saved task
     */
    Task save(Task task);
    
    /**
     * Find a task by ID
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
     * Find tasks by category
     * @param category the task category
     * @return list of tasks with the specified category
     */
    List<Task> findByCategory(String category);
    
    /**
     * Find overdue tasks
     * @return list of overdue tasks
     */
    List<Task> findOverdueTasks();
    
    /**
     * Find tasks due soon (within 24 hours)
     * @return list of tasks due soon
     */
    List<Task> findTasksDueSoon();
    
    /**
     * Find urgent tasks (high priority and due soon)
     * @return list of urgent tasks
     */
    List<Task> findUrgentTasks();
    
    /**
     * Delete a task by ID
     * @param id the task ID
     * @return true if the task was deleted
     */
    boolean deleteById(UUID id);
    
    /**
     * Check if a task exists by ID
     * @param id the task ID
     * @return true if the task exists
     */
    boolean existsById(UUID id);
    
    /**
     * Count all tasks
     * @return total number of tasks
     */
    long count();
    
    /**
     * Count tasks by status
     * @param status the task status
     * @return number of tasks with the specified status
     */
    long countByStatus(Task.TaskStatus status);
} 