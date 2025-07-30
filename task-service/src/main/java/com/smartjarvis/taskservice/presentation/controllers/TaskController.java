package com.smartjarvis.taskservice.presentation.controllers;

import com.smartjarvis.taskservice.application.dto.TaskDto;
import com.smartjarvis.taskservice.application.usecases.CreateTaskUseCase;
import com.smartjarvis.taskservice.application.usecases.GetTaskUseCase;
import com.smartjarvis.taskservice.domain.entities.Task.TaskPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Task Controller in the presentation layer
 * 
 * This controller handles HTTP requests and delegates business logic to use cases.
 * It follows the Single Responsibility Principle by only handling HTTP concerns.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    
    private final CreateTaskUseCase createTaskUseCase;
    private final GetTaskUseCase getTaskUseCase;
    
    public TaskController(CreateTaskUseCase createTaskUseCase, GetTaskUseCase getTaskUseCase) {
        this.createTaskUseCase = createTaskUseCase;
        this.getTaskUseCase = getTaskUseCase;
    }

    /**
     * Create a new task
     * Delegates to use case for business logic
     */
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody CreateTaskRequest request) {
        logger.info("Creating task with title: {}", request.getTitle());
        
        try {
            TaskDto task = createTaskUseCase.execute(
                request.getTitle(),
                request.getDescription(),
                request.getPriority(),
                request.getCategory(),
                request.getDueDate()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for task creation", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating task", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get task by ID
     * Delegates to use case for business logic
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable UUID id) {
        logger.info("Getting task with ID: {}", id);
        
        try {
            Optional<TaskDto> task = getTaskUseCase.execute(id);
            return task.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid task ID: {}", id, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error getting task with ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all tasks
     * Delegates to use case for business logic
     */
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        logger.info("Getting all tasks");
        
        try {
            List<TaskDto> tasks = getTaskUseCase.executeGetAll();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Error getting all tasks", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     * Simple health check without business logic
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("Health check requested");
        return ResponseEntity.ok("Task Service is running");
    }

    /**
     * Request DTO for creating tasks
     */
    public static class CreateTaskRequest {
        private String title;
        private String description;
        private TaskPriority priority;
        private String category;
        private LocalDateTime dueDate;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public TaskPriority getPriority() { return priority; }
        public void setPriority(TaskPriority priority) { this.priority = priority; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    }
} 