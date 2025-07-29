package com.smartjarvis.desktop.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartjarvis.desktop.domain.Task;
import java.time.LocalDateTime;
import java.util.UUID;

public class TaskDto {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("priority")
    private Task.TaskPriority priority;
    
    @JsonProperty("status")
    private Task.TaskStatus status;
    
    @JsonProperty("category")
    private String category;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dueDate")
    private LocalDateTime dueDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("completedAt")
    private LocalDateTime completedAt;
    
    // Constructors
    public TaskDto() {}
    
    public TaskDto(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.priority = task.getPriority();
        this.status = task.getStatus();
        this.category = task.getCategory();
        this.dueDate = task.getDueDate();
        this.createdAt = task.getCreatedAt();
        this.completedAt = task.getCompletedAt();
        // Note: Task domain doesn't have updatedAt, so we'll use createdAt as fallback
        this.updatedAt = task.getCreatedAt();
    }
    
    // Convert to Domain Entity
    public Task toDomain() {
        // Create task using the constructor that exists
        Task task = new Task(
            this.title != null ? this.title : "",
            this.description != null ? this.description : "",
            this.priority != null ? this.priority : Task.TaskPriority.MEDIUM,
            this.dueDate
        );
        
        // Set additional properties
        task.setCategory(this.category);
        
        // Note: We can't set id, createdAt, completedAt as they are final/private
        // The domain entity will handle these appropriately
        
        return task;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Task.TaskPriority getPriority() {
        return priority;
    }
    
    public void setPriority(Task.TaskPriority priority) {
        this.priority = priority;
    }
    
    public Task.TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(Task.TaskStatus status) {
        this.status = status;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}