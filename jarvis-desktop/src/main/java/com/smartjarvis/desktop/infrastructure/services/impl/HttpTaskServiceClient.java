package com.smartjarvis.desktop.infrastructure.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartjarvis.desktop.domain.Task;
import com.smartjarvis.desktop.infrastructure.dto.TaskDto;
import com.smartjarvis.desktop.infrastructure.services.TaskServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Real HTTP implementation of Task Service Client using WebClient
 */
public class HttpTaskServiceClient implements TaskServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpTaskServiceClient.class);
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    
    public HttpTaskServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
    
    @Override
    public Task createTask(Task task) {
        try {
            TaskDto taskDto = new TaskDto(task);
            
            return webClient.post()
                    .uri("/api/v1/tasks")
                    .bodyValue(taskDto)
                    .retrieve()
                    .bodyToMono(TaskDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .map(TaskDto::toDomain)
                    .doOnSuccess(result -> logger.info("Task created successfully: {}", result.getTitle()))
                    .doOnError(error -> logger.error("Failed to create task: {}", error.getMessage()))
                    .block();
        } catch (WebClientResponseException e) {
            logger.error("HTTP error creating task: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create task: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error creating task: {}", e.getMessage());
            throw new RuntimeException("Failed to create task", e);
        }
    }
    
    @Override
    public Task updateTask(Task task) {
        try {
            TaskDto taskDto = new TaskDto(task);
            
            return webClient.put()
                    .uri("/api/v1/tasks/{id}", task.getId())
                    .bodyValue(taskDto)
                    .retrieve()
                    .bodyToMono(TaskDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .map(TaskDto::toDomain)
                    .doOnSuccess(result -> logger.info("Task updated successfully: {}", result.getTitle()))
                    .doOnError(error -> logger.error("Failed to update task: {}", error.getMessage()))
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            logger.error("Task not found for update: {}", task.getId());
            throw new RuntimeException("Task not found: " + task.getId(), e);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error updating task: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to update task: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error updating task: {}", e.getMessage());
            throw new RuntimeException("Failed to update task", e);
        }
    }
    
    @Override
    public void deleteTask(UUID taskId) {
        try {
            webClient.delete()
                    .uri("/api/v1/tasks/{id}", taskId)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(result -> logger.info("Task deleted successfully: {}", taskId))
                    .doOnError(error -> logger.error("Failed to delete task: {}", error.getMessage()))
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            logger.error("Task not found for deletion: {}", taskId);
            throw new RuntimeException("Task not found: " + taskId, e);
        } catch (WebClientResponseException e) {
            logger.error("HTTP error deleting task: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to delete task: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error deleting task: {}", e.getMessage());
            throw new RuntimeException("Failed to delete task", e);
        }
    }
    
    @Override
    public Task getTask(UUID taskId) {
        try {
            return webClient.get()
                    .uri("/api/v1/tasks/{id}", taskId)
                    .retrieve()
                    .bodyToMono(TaskDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .map(TaskDto::toDomain)
                    .doOnSuccess(result -> logger.info("Task retrieved successfully: {}", result.getTitle()))
                    .doOnError(error -> logger.error("Failed to get task: {}", error.getMessage()))
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            logger.error("Task not found: {}", taskId);
            return null;
        } catch (WebClientResponseException e) {
            logger.error("HTTP error getting task: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get task: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error getting task: {}", e.getMessage());
            throw new RuntimeException("Failed to get task", e);
        }
    }
    
    @Override
    public List<Task> getAllTasks() {
        try {
            return webClient.get()
                    .uri("/api/v1/tasks")
                    .retrieve()
                    .bodyToMono(TaskDto[].class)
                    .timeout(Duration.ofSeconds(10))
                    .map(taskDtos -> java.util.Arrays.stream(taskDtos)
                            .map(TaskDto::toDomain)
                            .collect(Collectors.toList()))
                    .doOnSuccess(result -> logger.info("Retrieved {} tasks successfully", result.size()))
                    .doOnError(error -> logger.error("Failed to get all tasks: {}", error.getMessage()))
                    .block();
        } catch (WebClientResponseException e) {
            logger.error("HTTP error getting all tasks: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get all tasks: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error getting all tasks: {}", e.getMessage());
            throw new RuntimeException("Failed to get all tasks", e);
        }
    }
    
    /**
     * Test connection to the task service
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try {
            String response = webClient.get()
                    .uri("/api/v1/tasks/ping")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            logger.info("Task service connection test successful: {}", response);
            return true;
        } catch (Exception e) {
            logger.error("Task service connection test failed: {}", e.getMessage());
            return false;
        }
    }
}