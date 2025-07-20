package com.smartjarvis.desktop.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a task in the JARVIS system
 */
public class Task {
    private final UUID id;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDateTime dueDate;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String category;
    private String tags;

    public Task(String title, String description, TaskPriority priority, LocalDateTime dueDate) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TaskStatus.PENDING;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskPriority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public LocalDateTime getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public String getCategory() { return category; }
    public String getTags() { return tags; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public void setCategory(String category) { this.category = category; }
    public void setTags(String tags) { this.tags = tags; }

    // Business methods
    public void start() {
        if (status == TaskStatus.PENDING) {
            status = TaskStatus.IN_PROGRESS;
        }
    }

    public void complete() {
        status = TaskStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }

    public void pause() {
        if (status == TaskStatus.IN_PROGRESS) {
            status = TaskStatus.PAUSED;
        }
    }

    public void cancel() {
        status = TaskStatus.CANCELLED;
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && status != TaskStatus.COMPLETED;
    }

    public boolean isDueSoon() {
        if (dueDate == null || status == TaskStatus.COMPLETED) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        return dueDate.isBefore(tomorrow) && dueDate.isAfter(now);
    }

    public long getDaysUntilDue() {
        if (dueDate == null) return Long.MAX_VALUE;
        return java.time.Duration.between(LocalDateTime.now(), dueDate).toDays();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", dueDate=" + dueDate +
                '}';
    }

    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        CANCELLED
    }
} 