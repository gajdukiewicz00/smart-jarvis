package com.smartjarvis.taskservice.application.usecases;

import com.smartjarvis.taskservice.application.dto.TaskDto;
import com.smartjarvis.taskservice.domain.entities.Task;
import com.smartjarvis.taskservice.domain.services.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use Case for getting tasks
 * 
 * This use case orchestrates the business logic for retrieving tasks
 * and follows the Single Responsibility Principle.
 */
@Service
public class GetTaskUseCase {
    
    private final TaskService taskService;
    
    public GetTaskUseCase(TaskService taskService) {
        this.taskService = taskService;
    }
    
    /**
     * Executes the use case to get a task by ID
     * @param id task ID
     * @return Optional TaskDto
     */
    public Optional<TaskDto> execute(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        
        return taskService.getTaskById(id)
                .map(this::convertToDto);
    }
    
    /**
     * Executes the use case to get all tasks
     * @return List of TaskDto
     */
    public List<TaskDto> executeGetAll() {
        List<Task> tasks = taskService.getAllTasks();
        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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