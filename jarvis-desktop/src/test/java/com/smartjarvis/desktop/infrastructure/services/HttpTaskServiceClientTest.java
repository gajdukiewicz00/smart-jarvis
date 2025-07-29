package com.smartjarvis.desktop.infrastructure.services;

import com.smartjarvis.desktop.domain.Task;
import com.smartjarvis.desktop.infrastructure.services.impl.HttpTaskServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for HttpTaskServiceClient
 * Note: These tests require the task service to be running
 */
public class HttpTaskServiceClientTest {
    
    private HttpTaskServiceClient client;
    
    @BeforeEach
    void setUp() {
        client = new HttpTaskServiceClient("http://localhost:8082");
    }
    
    @Test
    void testConnection() {
        // Test connection to task service
        boolean connected = client.testConnection();
        System.out.println("Task service connection: " + connected);
        
        // This test will pass if service is running, fail gracefully if not
        if (!connected) {
            System.out.println("Warning: Task service is not running. Skipping HTTP tests.");
            return;
        }
    }
    
    @Test
    void testCreateAndGetTask() {
        // Skip if service is not available
        if (!client.testConnection()) {
            System.out.println("Skipping test - Task service not available");
            return;
        }
        
        // Create a test task
        Task testTask = new Task(
            "Test Task " + System.currentTimeMillis(),
            "Test Description",
            Task.TaskPriority.MEDIUM,
            LocalDateTime.now().plusDays(1)
        );
        
        // Create task via HTTP
        Task createdTask = client.createTask(testTask);
        assertNotNull(createdTask);
        assertNotNull(createdTask.getId());
        assertEquals(testTask.getTitle(), createdTask.getTitle());
        
        System.out.println("Created task: " + createdTask.getTitle() + " with ID: " + createdTask.getId());
        
        // Get task by ID
        Task retrievedTask = client.getTask(createdTask.getId());
        assertNotNull(retrievedTask);
        assertEquals(createdTask.getId(), retrievedTask.getId());
        assertEquals(createdTask.getTitle(), retrievedTask.getTitle());
        
        System.out.println("Retrieved task: " + retrievedTask.getTitle());
    }
    
    @Test
    void testGetAllTasks() {
        // Skip if service is not available
        if (!client.testConnection()) {
            System.out.println("Skipping test - Task service not available");
            return;
        }
        
        // Get all tasks
        List<Task> tasks = client.getAllTasks();
        assertNotNull(tasks);
        
        System.out.println("Retrieved " + tasks.size() + " tasks");
        for (Task task : tasks) {
            System.out.println("  - " + task.getTitle() + " (ID: " + task.getId() + ")");
        }
    }
    
    @Test
    void testUpdateTask() {
        // Skip if service is not available
        if (!client.testConnection()) {
            System.out.println("Skipping test - Task service not available");
            return;
        }
        
        // Create a test task
        Task testTask = new Task(
            "Update Test Task " + System.currentTimeMillis(),
            "Original Description",
            Task.TaskPriority.LOW,
            LocalDateTime.now().plusDays(1)
        );
        
        // Create task
        Task createdTask = client.createTask(testTask);
        assertNotNull(createdTask);
        
        // Update task
        createdTask.setTitle("Updated Task Title");
        createdTask.setDescription("Updated Description");
        createdTask.setPriority(Task.TaskPriority.HIGH);
        
        Task updatedTask = client.updateTask(createdTask);
        assertNotNull(updatedTask);
        assertEquals("Updated Task Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(Task.TaskPriority.HIGH, updatedTask.getPriority());
        
        System.out.println("Updated task: " + updatedTask.getTitle());
    }
    
    @Test
    void testDeleteTask() {
        // Skip if service is not available
        if (!client.testConnection()) {
            System.out.println("Skipping test - Task service not available");
            return;
        }
        
        // Create a test task
        Task testTask = new Task(
            "Delete Test Task " + System.currentTimeMillis(),
            "To be deleted",
            Task.TaskPriority.LOW,
            LocalDateTime.now().plusDays(1)
        );
        
        // Create task
        Task createdTask = client.createTask(testTask);
        assertNotNull(createdTask);
        UUID taskId = createdTask.getId();
        
        System.out.println("Created task for deletion: " + createdTask.getTitle() + " (ID: " + taskId + ")");
        
        // Delete task
        client.deleteTask(taskId);
        
        // Verify task is deleted
        Task deletedTask = client.getTask(taskId);
        assertNull(deletedTask);
        
        System.out.println("Successfully deleted task with ID: " + taskId);
    }
}