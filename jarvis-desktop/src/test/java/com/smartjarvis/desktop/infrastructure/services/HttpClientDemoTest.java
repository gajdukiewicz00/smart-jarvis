package com.smartjarvis.desktop.infrastructure.services;

import com.smartjarvis.desktop.domain.Task;
import com.smartjarvis.desktop.infrastructure.dto.TaskDto;
import com.smartjarvis.desktop.infrastructure.services.impl.HttpTaskServiceClient;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demo test showing HTTP client functionality
 */
public class HttpClientDemoTest {
    
    @Test
    void testTaskDtoConversion() {
        System.out.println("=== Testing Task DTO Conversion ===");
        
        // Create a domain task
        Task domainTask = new Task(
            "Test Task",
            "Test Description", 
            Task.TaskPriority.HIGH,
            LocalDateTime.now().plusDays(1)
        );
        domainTask.setCategory("Work");
        
        System.out.println("Domain Task: " + domainTask.getTitle());
        System.out.println("  - ID: " + domainTask.getId());
        System.out.println("  - Priority: " + domainTask.getPriority());
        System.out.println("  - Category: " + domainTask.getCategory());
        
        // Convert to DTO
        TaskDto taskDto = new TaskDto(domainTask);
        System.out.println("Task DTO: " + taskDto.getTitle());
        System.out.println("  - ID: " + taskDto.getId());
        System.out.println("  - Priority: " + taskDto.getPriority());
        System.out.println("  - Category: " + taskDto.getCategory());
        
        // Convert back to domain
        Task convertedTask = taskDto.toDomain();
        System.out.println("Converted Task: " + convertedTask.getTitle());
        System.out.println("  - Priority: " + convertedTask.getPriority());
        System.out.println("  - Category: " + convertedTask.getCategory());
        
        // Verify conversion
        assertEquals(domainTask.getTitle(), taskDto.getTitle());
        assertEquals(domainTask.getPriority(), taskDto.getPriority());
        assertEquals(domainTask.getCategory(), taskDto.getCategory());
        
        System.out.println("✅ DTO conversion test passed");
    }
    
    @Test
    void testHttpClientCreation() {
        System.out.println("=== Testing HTTP Client Creation ===");
        
        // Create HTTP client
        HttpTaskServiceClient client = new HttpTaskServiceClient("http://localhost:8081");
        assertNotNull(client);
        
        System.out.println("✅ HTTP client created successfully");
        System.out.println("Base URL: http://localhost:8081");
        
        // Test connection (will fail gracefully if service is down)
        boolean connected = client.testConnection();
        System.out.println("Connection test result: " + connected);
        
        if (connected) {
            System.out.println("✅ Task service is available");
        } else {
            System.out.println("⚠️ Task service is not available (this is expected in demo)");
        }
    }
    
    @Test
    void testServiceFactory() {
        System.out.println("=== Testing Service Factory ===");
        
        // Test service factory
        TaskServiceClient taskClient = ServiceFactory.createTaskServiceClient();
        assertNotNull(taskClient);
        assertTrue(taskClient instanceof HttpTaskServiceClient);
        
        NLPService nlpService = ServiceFactory.createNLPService();
        assertNotNull(nlpService);
        
        NotificationService notificationService = ServiceFactory.createNotificationService();
        assertNotNull(notificationService);
        
        SpeechService speechService = ServiceFactory.createSpeechService();
        assertNotNull(speechService);
        
        System.out.println("✅ All services created successfully via factory");
        System.out.println("  - TaskServiceClient: " + taskClient.getClass().getSimpleName());
        System.out.println("  - NLPService: " + nlpService.getClass().getSimpleName());
        System.out.println("  - NotificationService: " + notificationService.getClass().getSimpleName());
        System.out.println("  - SpeechService: " + speechService.getClass().getSimpleName());
    }
    
    @Test
    void testErrorHandling() {
        System.out.println("=== Testing Error Handling ===");
        
        // Create client with invalid URL
        HttpTaskServiceClient client = new HttpTaskServiceClient("http://invalid-url:9999");
        
        // Test connection to invalid URL
        boolean connected = client.testConnection();
        assertFalse(connected);
        
        System.out.println("✅ Error handling test passed");
        System.out.println("  - Invalid URL handled gracefully");
        System.out.println("  - Connection test returned false as expected");
    }
}