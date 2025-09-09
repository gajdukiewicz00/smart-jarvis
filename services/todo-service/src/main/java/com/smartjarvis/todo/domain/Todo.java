package com.smartjarvis.todo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Todo entity for MongoDB storage
 */
@Document(collection = "todos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo {

    @Id
    private String id;

    /**
     * User who owns this todo
     */
    private String userId;

    /**
     * Todo title/summary
     */
    private String title;

    /**
     * Detailed description
     */
    private String description;

    /**
     * Due date and time
     */
    private LocalDateTime dueDate;

    /**
     * Current status
     */
    @Builder.Default
    private TodoStatus status = TodoStatus.PENDING;

    /**
     * Priority level
     */
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    /**
     * Tags for categorization
     */
    private String[] tags;

    /**
     * Creation timestamp
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Completion timestamp
     */
    private LocalDateTime completedAt;

    /**
     * Mark todo as completed
     */
    public void markCompleted() {
        this.status = TodoStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Check if todo is overdue
     */
    public boolean isOverdue() {
        return dueDate != null && 
               status != TodoStatus.COMPLETED && 
               LocalDateTime.now().isAfter(dueDate);
    }

    /**
     * Check if todo is due today
     */
    public boolean isDueToday() {
        if (dueDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dueDate.toLocalDate().equals(now.toLocalDate());
    }
}
