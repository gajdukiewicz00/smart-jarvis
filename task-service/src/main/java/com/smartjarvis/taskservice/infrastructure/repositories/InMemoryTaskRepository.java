package com.smartjarvis.taskservice.infrastructure.repositories;

import com.smartjarvis.taskservice.domain.entities.Task;
import com.smartjarvis.taskservice.domain.repositories.TaskRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of TaskRepository
 * 
 * This implementation is used for testing and development purposes.
 * It stores tasks in a ConcurrentHashMap for thread safety.
 */
@Repository
public class InMemoryTaskRepository implements TaskRepository {
    
    private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();
    
    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(UUID.randomUUID());
        }
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
    public List<Task> findByCategory(String category) {
        return tasks.values().stream()
                .filter(task -> Objects.equals(task.getCategory(), category))
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
    public List<Task> findUrgentTasks() {
        return tasks.values().stream()
                .filter(task -> task.isUrgent())
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean deleteById(UUID id) {
        return tasks.remove(id) != null;
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