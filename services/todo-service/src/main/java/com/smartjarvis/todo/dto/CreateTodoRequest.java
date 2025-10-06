package com.smartjarvis.todo.dto;

import com.smartjarvis.todo.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating new todo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTodoRequest {

    /**
     * User ID who owns the todo
     */
    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    /**
     * Todo title
     */
    @NotBlank(message = "Title cannot be blank")
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
     * Priority level
     */
    private Priority priority;

    /**
     * Tags for categorization
     */
    private String[] tags;
}
