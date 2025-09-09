package com.smartjarvis.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for todo statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoStatsResponse {

    /**
     * Total number of todos
     */
    private long totalTodos;

    /**
     * Number of pending todos
     */
    private long pendingCount;

    /**
     * Number of in-progress todos
     */
    private long inProgressCount;

    /**
     * Number of completed todos
     */
    private long completedCount;

    /**
     * Number of overdue todos
     */
    private long overdueCount;

    /**
     * Completion rate (0.0 to 1.0)
     */
    public double getCompletionRate() {
        if (totalTodos == 0) return 0.0;
        return (double) completedCount / totalTodos;
    }

    /**
     * Overdue rate (0.0 to 1.0)
     */
    public double getOverdueRate() {
        if (totalTodos == 0) return 0.0;
        return (double) overdueCount / totalTodos;
    }
}
