package com.smartjarvis.todo.dto;

import com.smartjarvis.todo.domain.Priority;
import com.smartjarvis.todo.domain.TodoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for todo operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoResponse {

    /**
     * Todo ID
     */
    private String id;

    /**
     * User ID who owns the todo
     */
    private String userId;

    /**
     * Todo title
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
    private TodoStatus status;

    /**
     * Priority level
     */
    private Priority priority;

    /**
     * Tags for categorization
     */
    private String[] tags;

    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Completion timestamp
     */
    private LocalDateTime completedAt;

    /**
     * Whether todo is overdue
     */
    private boolean isOverdue;

    /**
     * Whether todo is due today
     */
    private boolean isDueToday;
}
