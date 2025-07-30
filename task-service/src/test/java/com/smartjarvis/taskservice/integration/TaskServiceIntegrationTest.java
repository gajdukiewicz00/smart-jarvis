package com.smartjarvis.taskservice.integration;

import com.smartjarvis.taskservice.application.dto.TaskDto;
import com.smartjarvis.taskservice.application.usecases.CreateTaskUseCase;
import com.smartjarvis.taskservice.application.usecases.GetTaskUseCase;
import com.smartjarvis.taskservice.domain.entities.Task;
import com.smartjarvis.taskservice.domain.services.TaskService;
import com.smartjarvis.taskservice.infrastructure.services.TaskServiceImpl;
import com.smartjarvis.taskservice.infrastructure.repositories.InMemoryTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Integration test for Task Service
 * 
 * This test verifies that all layers work together correctly
 * and follows the Clean Architecture principles.
 */
class TaskServiceIntegrationTest {
    
    private TaskService taskService;
    private CreateTaskUseCase createTaskUseCase;
    private GetTaskUseCase getTaskUseCase;
    private InMemoryTaskRepository taskRepository;
    
    @BeforeEach
    void setUp() {
        taskRepository = new InMemoryTaskRepository();
        taskService = new TaskServiceImpl(taskRepository);
        createTaskUseCase = new CreateTaskUseCase(taskService);
        getTaskUseCase = new GetTaskUseCase(taskService);
    }
    
    @Test
    void testCreateAndRetrieveTask() {
        // Given
        String title = "Integration Test Task";
        String description = "Test Description";
        Task.TaskPriority priority = Task.TaskPriority.HIGH;
        String category = "Test Category";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        
        // When
        TaskDto createdTask = createTaskUseCase.execute(title, description, priority, category, dueDate);
        
        // Then
        assertNotNull(createdTask);
        assertEquals(title, createdTask.getTitle());
        assertEquals(description, createdTask.getDescription());
        assertEquals(priority, createdTask.getPriority());
        assertEquals(category, createdTask.getCategory());
        assertEquals(Task.TaskStatus.PENDING, createdTask.getStatus());
        
        // Verify retrieval
        Optional<TaskDto> retrievedTask = getTaskUseCase.execute(createdTask.getId());
        assertTrue(retrievedTask.isPresent());
        assertEquals(createdTask.getId(), retrievedTask.get().getId());
    }
    
    @Test
    void testGetAllTasks() {
        // Given
        createTaskUseCase.execute("Task 1", "Description 1", Task.TaskPriority.LOW, "Category 1", LocalDateTime.now().plusDays(1));
        createTaskUseCase.execute("Task 2", "Description 2", Task.TaskPriority.MEDIUM, "Category 2", LocalDateTime.now().plusDays(2));
        
        // When
        List<TaskDto> allTasks = getTaskUseCase.executeGetAll();
        
        // Then
        assertEquals(2, allTasks.size());
        assertTrue(allTasks.stream().anyMatch(task -> task.getTitle().equals("Task 1")));
        assertTrue(allTasks.stream().anyMatch(task -> task.getTitle().equals("Task 2")));
    }
    
    @Test
    void testGetNonExistentTask() {
        // When
        Optional<TaskDto> task = getTaskUseCase.execute(UUID.randomUUID());
        
        // Then
        assertFalse(task.isPresent());
    }
    
    @Test
    void testTaskBusinessLogic() {
        // Given
        TaskDto task = createTaskUseCase.execute(
            "Business Logic Test",
            "Test Description",
            Task.TaskPriority.URGENT,
            "Test Category",
            LocalDateTime.now().plusHours(12) // Due soon
        );
        
        // When
        Optional<TaskDto> retrievedTask = getTaskUseCase.execute(task.getId());
        
        // Then
        assertTrue(retrievedTask.isPresent());
        TaskDto taskDto = retrievedTask.get();
        
        // Verify business logic - check due date logic
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = taskDto.getDueDate();
        boolean isDueSoon = dueDate != null && dueDate.isBefore(now.plusHours(24)) && dueDate.isAfter(now);
        boolean isOverdue = dueDate != null && dueDate.isBefore(now);
        
        assertTrue(isDueSoon); // Should be due soon (within 24 hours)
        assertFalse(isOverdue); // Should not be overdue
        assertEquals(Task.TaskStatus.PENDING, taskDto.getStatus()); // Should start as pending
    }
} 