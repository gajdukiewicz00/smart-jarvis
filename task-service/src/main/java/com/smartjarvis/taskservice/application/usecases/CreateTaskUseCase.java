package com.smartjarvis.taskservice.application.usecases;

import com.smartjarvis.taskservice.application.dto.TaskDto;
import com.smartjarvis.taskservice.domain.entities.Task;
import com.smartjarvis.taskservice.domain.entities.Task.TaskPriority;
import com.smartjarvis.taskservice.domain.services.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Use Case for creating a new task
 * 
 * This use case orchestrates the business logic for creating tasks
 * and follows the Single Responsibility Principle.
 */
@Service
public class CreateTaskUseCase {
    
    private final TaskService taskService;
    
    public CreateTaskUseCase(TaskService taskService) {
        this.taskService = taskService;
    }
    
    /**
     * Executes the use case to create a new task
     * @param title task title
     * @param description task description
     * @param priority task priority
     * @param category task category
     * @param dueDate task due date (optional)
     * @return TaskDto of the created task
     */
    public TaskDto execute(String title, String description, TaskPriority priority, String category, LocalDateTime dueDate) {
        // Validate input parameters
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        
        if (priority == null) {
            priority = TaskPriority.MEDIUM;
        }
        
        // Validate task data
        if (!taskService.validateTaskData(title, description, priority)) {
            throw new IllegalArgumentException("Invalid task data");
        }
        
        // Create domain entity
        Task task = taskService.createTask(title, description, priority, category, dueDate);
        
        // Convert to DTO
        return convertToDto(task);
    }
    
    /**
     * Converts domain entity to DTO
     * @param task the domain entity
     * @return TaskDto
     */
    private TaskDto convertToDto(Task task) {
        return new TaskDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getPriority(),
            task.getStatus(),
            task.getCategory(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getCompletedAt()
        );
    }
} 