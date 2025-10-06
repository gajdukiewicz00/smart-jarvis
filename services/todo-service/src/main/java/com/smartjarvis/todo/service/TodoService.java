package com.smartjarvis.todo.service;

import com.smartjarvis.todo.domain.Priority;
import com.smartjarvis.todo.domain.Todo;
import com.smartjarvis.todo.domain.TodoStatus;
import com.smartjarvis.todo.dto.CreateTodoRequest;
import com.smartjarvis.todo.dto.TodoResponse;
import com.smartjarvis.todo.dto.UpdateTodoRequest;
import com.smartjarvis.todo.exception.TodoNotFoundException;
import com.smartjarvis.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.smartjarvis.todo.dto.TodoStatsResponse;

/**
 * Todo service with business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Create new todo
     */
    public TodoResponse createTodo(CreateTodoRequest request) {
        log.info("Creating todo: title='{}', userId='{}'", request.getTitle(), request.getUserId());

        Todo todo = Todo.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.NORMAL)
                .tags(request.getTags())
                .build();

        Todo savedTodo = todoRepository.save(todo);

        // Publish todo created event
        publishTodoCreatedEvent(savedTodo);

        log.info("Todo created successfully: id='{}'", savedTodo.getId());
        return mapToResponse(savedTodo);
    }

    /**
     * Get todos for user
     */
    public List<TodoResponse> getTodosByUserId(String userId) {
        log.debug("Getting todos for user: '{}'", userId);
        
        List<Todo> todos = todoRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return todos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get todos with pagination
     */
    public Page<TodoResponse> getTodosByUserId(String userId, Pageable pageable) {
        log.debug("Getting todos for user: '{}' with pagination", userId);
        
        Page<Todo> todos = todoRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return todos.map(this::mapToResponse);
    }

    /**
     * Get todo by ID
     */
    public TodoResponse getTodoById(String id, String userId) {
        log.debug("Getting todo: id='{}', userId='{}'", id, userId);
        
        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TodoNotFoundException(id));
                
        return mapToResponse(todo);
    }

    /**
     * Update todo
     */
    public TodoResponse updateTodo(String id, UpdateTodoRequest request, String userId) {
        log.info("Updating todo: id='{}', userId='{}'", id, userId);

        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TodoNotFoundException(id));

        // Update fields
        if (request.getTitle() != null) {
            todo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            todo.setDescription(request.getDescription());
        }
        if (request.getDueDate() != null) {
            todo.setDueDate(request.getDueDate());
        }
        if (request.getPriority() != null) {
            todo.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            todo.setStatus(request.getStatus());
            if (request.getStatus() == TodoStatus.COMPLETED) {
                todo.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.getTags() != null) {
            todo.setTags(request.getTags());
        }

        Todo updatedTodo = todoRepository.save(todo);

        // Publish todo updated event
        publishTodoUpdatedEvent(updatedTodo);

        log.info("Todo updated successfully: id='{}'", updatedTodo.getId());
        return mapToResponse(updatedTodo);
    }

    /**
     * Mark todo as completed
     */
    public TodoResponse markCompleted(String id, String userId) {
        log.info("Marking todo as completed: id='{}', userId='{}'", id, userId);

        Todo todo = todoRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TodoNotFoundException(id));

        todo.markCompleted();
        Todo completedTodo = todoRepository.save(todo);

        // Publish todo completed event
        publishTodoCompletedEvent(completedTodo);

        log.info("Todo marked as completed: id='{}'", completedTodo.getId());
        return mapToResponse(completedTodo);
    }

    /**
     * Delete todo
     */
    public void deleteTodo(String id, String userId) {
        log.info("Deleting todo: id='{}', userId='{}'", id, userId);

        if (!todoRepository.existsByIdAndUserId(id, userId)) {
            throw new TodoNotFoundException(id);
        }

        todoRepository.deleteByIdAndUserId(id, userId);

        // Publish todo deleted event
        publishTodoDeletedEvent(id, userId);

        log.info("Todo deleted successfully: id='{}'", id);
    }

    /**
     * Get pending todos for user
     */
    public List<TodoResponse> getPendingTodos(String userId) {
        List<Todo> todos = todoRepository.findPendingTodosByUserId(userId);
        return todos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get todos due today
     */
    public List<TodoResponse> getTodosDueToday(String userId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        List<Todo> todos = todoRepository.findTodosDueToday(userId, startOfDay, endOfDay);
        return todos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue todos
     */
    public List<TodoResponse> getOverdueTodos(String userId) {
        List<Todo> todos = todoRepository.findOverdueTodos(userId, LocalDateTime.now());
        return todos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get todo statistics
     */
    public TodoStatsResponse getTodoStats(String userId) {
        long pendingCount = todoRepository.countByUserIdAndStatus(userId, TodoStatus.PENDING);
        long inProgressCount = todoRepository.countByUserIdAndStatus(userId, TodoStatus.IN_PROGRESS);
        long completedCount = todoRepository.countByUserIdAndStatus(userId, TodoStatus.COMPLETED);
        
        List<Todo> overdueTodos = todoRepository.findOverdueTodos(userId, LocalDateTime.now());
        
        return TodoStatsResponse.builder()
                .totalTodos(pendingCount + inProgressCount + completedCount)
                .pendingCount(pendingCount)
                .inProgressCount(inProgressCount)
                .completedCount(completedCount)
                .overdueCount(overdueTodos.size())
                .build();
    }

    /**
     * Map Todo entity to response DTO
     */
    private TodoResponse mapToResponse(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .userId(todo.getUserId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .dueDate(todo.getDueDate())
                .status(todo.getStatus())
                .priority(todo.getPriority())
                .tags(todo.getTags())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .completedAt(todo.getCompletedAt())
                .isOverdue(todo.isOverdue())
                .isDueToday(todo.isDueToday())
                .build();
    }

    /**
     * Publish todo created event to Kafka
     */
    private void publishTodoCreatedEvent(Todo todo) {
        try {
            // TODO: Create proper Avro event
            kafkaTemplate.send("todo.created", todo.getId(), todo);
            log.debug("Todo created event published: id='{}'", todo.getId());
        } catch (Exception e) {
            log.error("Failed to publish todo created event: id='{}'", todo.getId(), e);
        }
    }

    /**
     * Publish todo updated event to Kafka
     */
    private void publishTodoUpdatedEvent(Todo todo) {
        try {
            kafkaTemplate.send("todo.updated", todo.getId(), todo);
            log.debug("Todo updated event published: id='{}'", todo.getId());
        } catch (Exception e) {
            log.error("Failed to publish todo updated event: id='{}'", todo.getId(), e);
        }
    }

    /**
     * Publish todo completed event to Kafka
     */
    private void publishTodoCompletedEvent(Todo todo) {
        try {
            kafkaTemplate.send("todo.completed", todo.getId(), todo);
            log.debug("Todo completed event published: id='{}'", todo.getId());
        } catch (Exception e) {
            log.error("Failed to publish todo completed event: id='{}'", todo.getId(), e);
        }
    }

    /**
     * Publish todo deleted event to Kafka
     */
    private void publishTodoDeletedEvent(String todoId, String userId) {
        try {
            kafkaTemplate.send("todo.deleted", todoId, Map.of("todoId", todoId, "userId", userId));
            log.debug("Todo deleted event published: id='{}'", todoId);
        } catch (Exception e) {
            log.error("Failed to publish todo deleted event: id='{}'", todoId, e);
        }
    }
}
