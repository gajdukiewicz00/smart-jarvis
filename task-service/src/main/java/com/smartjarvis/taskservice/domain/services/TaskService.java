package com.smartjarvis.taskservice.domain.services;

import com.smartjarvis.taskservice.domain.entities.Task;
import com.smartjarvis.taskservice.domain.repositories.TaskRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service for Task business logic
 * 
 * This service contains business rules and operations that don't belong
 * to individual entities but are part of the domain logic.
 */
public interface TaskService {
    
    /**
     * Create a new task
     * @param title task title
     * @param description task description
     * @param priority task priority
     * @param category task category
     * @param dueDate task due date
     * @return the created task
     */
    Task createTask(String title, String description, Task.TaskPriority priority, 
                   String category, java.time.LocalDateTime dueDate);
    
    /**
     * Get a task by ID
     * @param id task ID
     * @return Optional containing the task if found
     */
    Optional<Task> getTaskById(UUID id);
    
    /**
     * Get all tasks
     * @return list of all tasks
     */
    List<Task> getAllTasks();
    
    /**
     * Update a task
     * @param id task ID
     * @param title new title
     * @param description new description
     * @param priority new priority
     * @param category new category
     * @param dueDate new due date
     * @return Optional containing the updated task if found
     */
    Optional<Task> updateTask(UUID id, String title, String description, 
                             Task.TaskPriority priority, String category, 
                             java.time.LocalDateTime dueDate);
    
    /**
     * Delete a task
     * @param id task ID
     * @return true if the task was deleted
     */
    boolean deleteTask(UUID id);
    
    /**
     * Start working on a task
     * @param id task ID
     * @return Optional containing the updated task if found
     */
    Optional<Task> startTask(UUID id);
    
    /**
     * Pause a task
     * @param id task ID
     * @return Optional containing the updated task if found
     */
    Optional<Task> pauseTask(UUID id);
    
    /**
     * Complete a task
     * @param id task ID
     * @return Optional containing the updated task if found
     */
    Optional<Task> completeTask(UUID id);
    
    /**
     * Cancel a task
     * @param id task ID
     * @return Optional containing the updated task if found
     */
    Optional<Task> cancelTask(UUID id);
    
    /**
     * Get tasks by status
     * @param status task status
     * @return list of tasks with the specified status
     */
    List<Task> getTasksByStatus(Task.TaskStatus status);
    
    /**
     * Get tasks by priority
     * @param priority task priority
     * @return list of tasks with the specified priority
     */
    List<Task> getTasksByPriority(Task.TaskPriority priority);
    
    /**
     * Get tasks by category
     * @param category task category
     * @return list of tasks with the specified category
     */
    List<Task> getTasksByCategory(String category);
    
    /**
     * Get overdue tasks
     * @return list of overdue tasks
     */
    List<Task> getOverdueTasks();
    
    /**
     * Get tasks due soon (within 24 hours)
     * @return list of tasks due soon
     */
    List<Task> getTasksDueSoon();
    
    /**
     * Get urgent tasks (high priority and due soon)
     * @return list of urgent tasks
     */
    List<Task> getUrgentTasks();
    
    /**
     * Get task statistics
     * @return TaskStatistics object with various metrics
     */
    TaskStatistics getTaskStatistics();
    
    /**
     * Check if task exists
     * @param id task ID
     * @return true if the task exists
     */
    boolean taskExists(UUID id);
    
    /**
     * Validate task data
     * @param title task title
     * @param description task description
     * @param priority task priority
     * @return true if the data is valid
     */
    boolean validateTaskData(String title, String description, Task.TaskPriority priority);
    
    /**
     * Inner class for task statistics
     */
    class TaskStatistics {
        private final long totalTasks;
        private final long pendingTasks;
        private final long inProgressTasks;
        private final long completedTasks;
        private final long overdueTasks;
        private final long urgentTasks;
        
        public TaskStatistics(long totalTasks, long pendingTasks, long inProgressTasks, 
                           long completedTasks, long overdueTasks, long urgentTasks) {
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.inProgressTasks = inProgressTasks;
            this.completedTasks = completedTasks;
            this.overdueTasks = overdueTasks;
            this.urgentTasks = urgentTasks;
        }
        
        // Getters
        public long getTotalTasks() { return totalTasks; }
        public long getPendingTasks() { return pendingTasks; }
        public long getInProgressTasks() { return inProgressTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getOverdueTasks() { return overdueTasks; }
        public long getUrgentTasks() { return urgentTasks; }
        
        @Override
        public String toString() {
            return "TaskStatistics{" +
                    "totalTasks=" + totalTasks +
                    ", pendingTasks=" + pendingTasks +
                    ", inProgressTasks=" + inProgressTasks +
                    ", completedTasks=" + completedTasks +
                    ", overdueTasks=" + overdueTasks +
                    ", urgentTasks=" + urgentTasks +
                    '}';
        }
    }
} 