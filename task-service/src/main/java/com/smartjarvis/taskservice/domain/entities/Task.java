package com.smartjarvis.taskservice.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity for Task
 * 
 * This entity contains business logic and represents the core domain model.
 * It follows the Single Responsibility Principle and encapsulates task-related behavior.
 */
public class Task {
    
    public enum TaskPriority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum TaskStatus {
        PENDING, IN_PROGRESS, PAUSED, COMPLETED, CANCELLED
    }
    
    private UUID id;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private String category;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    // Default constructor
    public Task() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = TaskStatus.PENDING;
        this.priority = TaskPriority.MEDIUM;
    }
    
    // Constructor with required fields
    public Task(String title, String description, TaskPriority priority, String category, LocalDateTime dueDate) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.dueDate = dueDate;
    }
    
    // Constructor with all fields
    public Task(UUID id, String title, String description, TaskPriority priority, 
                TaskStatus status, String category, LocalDateTime dueDate, 
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime completedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.category = category;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }
    
    // Business logic methods
    
    /**
     * Start working on the task
     */
    public void start() {
        if (this.status == TaskStatus.PENDING || this.status == TaskStatus.PAUSED) {
            this.status = TaskStatus.IN_PROGRESS;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot start task with status: " + this.status);
        }
    }
    
    /**
     * Pause the task
     */
    public void pause() {
        if (this.status == TaskStatus.IN_PROGRESS) {
            this.status = TaskStatus.PAUSED;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot pause task with status: " + this.status);
        }
    }
    
    /**
     * Complete the task
     */
    public void complete() {
        if (this.status != TaskStatus.CANCELLED) {
            this.status = TaskStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot complete cancelled task");
        }
    }
    
    /**
     * Cancel the task
     */
    public void cancel() {
        if (this.status != TaskStatus.COMPLETED) {
            this.status = TaskStatus.CANCELLED;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot cancel completed task");
        }
    }
    
    /**
     * Check if task is overdue
     */
    public boolean isOverdue() {
        return this.dueDate != null && 
               LocalDateTime.now().isAfter(this.dueDate) && 
               this.status != TaskStatus.COMPLETED;
    }
    
    /**
     * Check if task is due soon (within 24 hours)
     */
    public boolean isDueSoon() {
        if (this.dueDate == null || this.status == TaskStatus.COMPLETED) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        return this.dueDate.isAfter(now) && this.dueDate.isBefore(tomorrow);
    }
    
    /**
     * Check if task is urgent (high priority and due soon)
     */
    public boolean isUrgent() {
        return (this.priority == TaskPriority.HIGH || this.priority == TaskPriority.URGENT) && 
               this.isDueSoon();
    }
    
    /**
     * Update task details
     */
    public void update(String title, String description, TaskPriority priority, String category, LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.dueDate = dueDate;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Validate task data
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               priority != null && status != null;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", category='" + category + '\'' +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", completedAt=" + completedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id.equals(task.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
} 