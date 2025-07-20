package com.smartjarvis.desktop.infrastructure.repositories;

import com.smartjarvis.desktop.domain.Task;
import com.smartjarvis.desktop.domain.repositories.TaskRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of Task Repository
 */
public class InMemoryTaskRepository implements TaskRepository {
    
    private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();
    
    @Override
    public Task save(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }
    
    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(tasks.get(id));
    }
    
    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }
    
    @Override
    public List<Task> findByStatus(Task.TaskStatus status) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findByPriority(Task.TaskPriority priority) {
        return tasks.values().stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return tasks.values().stream()
                .filter(task -> task.isOverdue())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findTasksDueSoon() {
        return tasks.values().stream()
                .filter(task -> task.isDueSoon())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findByCategory(String category) {
        return tasks.values().stream()
                .filter(task -> category.equals(task.getCategory()))
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(UUID id) {
        tasks.remove(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return tasks.containsKey(id);
    }
    
    @Override
    public long count() {
        return tasks.size();
    }
    
    @Override
    public long countByStatus(Task.TaskStatus status) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }
} 