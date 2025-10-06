package com.smartjarvis.todo.dto;

import com.smartjarvis.todo.domain.Priority;
import com.smartjarvis.todo.domain.TodoStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for updating existing todo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTodoRequest {

    /**
     * Todo title
     */
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /**
     * Detailed description
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * Due date and time
     */
    private LocalDateTime dueDate;

    /**
     * Todo status
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
}
