package com.smartjarvis.taskservice.controller;

import com.smartjarvis.taskservice.domain.Task;
import com.smartjarvis.taskservice.dto.TaskDto;
import com.smartjarvis.taskservice.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    private final TaskService taskService;
    
    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
    
    // Create task
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        TaskDto createdTask = taskService.createTask(taskDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }
    
    // Get all tasks
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        List<TaskDto> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }
    
    // Get task by ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable UUID id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Update task
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable UUID id, @Valid @RequestBody TaskDto taskDto) {
        return taskService.updateTask(id, taskDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        boolean deleted = taskService.deleteTask(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // Complete task
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskDto> completeTask(@PathVariable UUID id) {
        return taskService.completeTask(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Start task
    @PatchMapping("/{id}/start")
    public ResponseEntity<TaskDto> startTask(@PathVariable UUID id) {
        return taskService.startTask(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Pause task
    @PatchMapping("/{id}/pause")
    public ResponseEntity<TaskDto> pauseTask(@PathVariable UUID id) {
        return taskService.pauseTask(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Get tasks by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDto>> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        List<TaskDto> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }
    
    // Get tasks by priority
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskDto>> getTasksByPriority(@PathVariable Task.TaskPriority priority) {
        List<TaskDto> tasks = taskService.getTasksByPriority(priority);
        return ResponseEntity.ok(tasks);
    }
    
    // Get tasks by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TaskDto>> getTasksByCategory(@PathVariable String category) {
        List<TaskDto> tasks = taskService.getTasksByCategory(category);
        return ResponseEntity.ok(tasks);
    }
    
    // Get overdue tasks
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskDto>> getOverdueTasks() {
        List<TaskDto> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }
    
    // Get tasks due soon
    @GetMapping("/due-soon")
    public ResponseEntity<List<TaskDto>> getTasksDueSoon() {
        List<TaskDto> tasks = taskService.getTasksDueSoon();
        return ResponseEntity.ok(tasks);
    }
    
    // Search tasks by title
    @GetMapping("/search/title")
    public ResponseEntity<List<TaskDto>> searchTasksByTitle(@RequestParam String title) {
        List<TaskDto> tasks = taskService.searchTasksByTitle(title);
        return ResponseEntity.ok(tasks);
    }
    
    // Search tasks by description
    @GetMapping("/search/description")
    public ResponseEntity<List<TaskDto>> searchTasksByDescription(@RequestParam String description) {
        List<TaskDto> tasks = taskService.searchTasksByDescription(description);
        return ResponseEntity.ok(tasks);
    }
    
    // Get task statistics
    @GetMapping("/statistics")
    public ResponseEntity<TaskService.TaskStatistics> getTaskStatistics() {
        TaskService.TaskStatistics statistics = taskService.getTaskStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Task Service is running!");
    }
    
    // Simple test endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Task Service test endpoint is working!");
    }
    

    
    // Database test endpoint
    @GetMapping("/db-test")
    public ResponseEntity<String> dbTest() {
        try {
            long count = taskService.getAllTasks().size();
            return ResponseEntity.ok("Database connected! Tasks count: " + count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database error: " + e.getMessage());
        }
    }
    
    // Simple controller test endpoint
    @GetMapping("/controller-test")
    public ResponseEntity<String> controllerTest() {
        return ResponseEntity.ok("TaskController is working!");
    }
} 