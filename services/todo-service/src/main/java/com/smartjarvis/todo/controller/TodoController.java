package com.smartjarvis.todo.controller;

import com.smartjarvis.todo.dto.*;
import com.smartjarvis.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Todo operations
 */
@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
@Slf4j
public class TodoController {

    private final TodoService todoService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "todo-service",
            "version", "1.0.0-SNAPSHOT",
            "timestamp", Instant.now().toString()
        ));
    }

    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        log.info("Creating todo: title='{}', userId='{}'", request.getTitle(), request.getUserId());
        
        TodoResponse response = todoService.createTodo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getTodos(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting todos for user: '{}', page: {}, size: {}", userId, page, size);
        
        if (page >= 0 && size > 0) {
            Pageable pageable = PageRequest.of(page, size);
            Page<TodoResponse> todosPage = todoService.getTodosByUserId(userId, pageable);
            return ResponseEntity.ok(todosPage.getContent());
        } else {
            List<TodoResponse> todos = todoService.getTodosByUserId(userId);
            return ResponseEntity.ok(todos);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodo(
            @PathVariable String id,
            @RequestParam String userId) {
        
        log.debug("Getting todo: id='{}', userId='{}'", id, userId);
        
        TodoResponse response = todoService.getTodoById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable String id,
            @Valid @RequestBody UpdateTodoRequest request,
            @RequestParam String userId) {
        
        log.info("Updating todo: id='{}', userId='{}'", id, userId);
        
        TodoResponse response = todoService.updateTodo(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable String id,
            @RequestParam String userId) {
        
        log.info("Deleting todo: id='{}', userId='{}'", id, userId);
        
        todoService.deleteTodo(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TodoResponse> markCompleted(
            @PathVariable String id,
            @RequestParam String userId) {
        
        log.info("Marking todo as completed: id='{}', userId='{}'", id, userId);
        
        TodoResponse response = todoService.markCompleted(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<TodoResponse>> getPendingTodos(@RequestParam String userId) {
        log.debug("Getting pending todos for user: '{}'", userId);
        
        List<TodoResponse> todos = todoService.getPendingTodos(userId);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/due-today")
    public ResponseEntity<List<TodoResponse>> getTodosDueToday(@RequestParam String userId) {
        log.debug("Getting todos due today for user: '{}'", userId);
        
        List<TodoResponse> todos = todoService.getTodosDueToday(userId);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TodoResponse>> getOverdueTodos(@RequestParam String userId) {
        log.debug("Getting overdue todos for user: '{}'", userId);
        
        List<TodoResponse> todos = todoService.getOverdueTodos(userId);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/stats")
    public ResponseEntity<TodoStatsResponse> getTodoStats(@RequestParam String userId) {
        log.debug("Getting todo statistics for user: '{}'", userId);
        
        TodoStatsResponse stats = todoService.getTodoStats(userId);
        return ResponseEntity.ok(stats);
    }
}
