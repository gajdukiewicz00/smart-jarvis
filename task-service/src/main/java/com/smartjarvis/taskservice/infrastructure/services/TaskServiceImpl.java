package com.smartjarvis.taskservice.infrastructure.services;

import com.smartjarvis.taskservice.domain.entities.Task;
import com.smartjarvis.taskservice.domain.repositories.TaskRepository;
import com.smartjarvis.taskservice.domain.services.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of TaskService
 * 
 * This service implements the domain service interface and coordinates
 * between the domain layer and infrastructure layer.
 */
@Service
public class TaskServiceImpl implements TaskService {
    
    private final TaskRepository taskRepository;
    
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    @Override
    public Task createTask(String title, String description, Task.TaskPriority priority, 
                          String category, LocalDateTime dueDate) {
        Task task = new Task(title, description, priority, category, dueDate);
        return taskRepository.save(task);
    }
    
    @Override
    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findById(id);
    }
    
    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    @Override
    public Optional<Task> updateTask(UUID id, String title, String description, 
                                   Task.TaskPriority priority, String category, LocalDateTime dueDate) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.update(title, description, priority, category, dueDate);
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public boolean deleteTask(UUID id) {
        return taskRepository.deleteById(id);
    }
    
    @Override
    public Optional<Task> startTask(UUID id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.start();
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public Optional<Task> pauseTask(UUID id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.pause();
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public Optional<Task> completeTask(UUID id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.complete();
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public Optional<Task> cancelTask(UUID id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.cancel();
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }
    
    @Override
    public List<Task> getTasksByPriority(Task.TaskPriority priority) {
        return taskRepository.findByPriority(priority);
    }
    
    @Override
    public List<Task> getTasksByCategory(String category) {
        return taskRepository.findByCategory(category);
    }
    
    @Override
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks();
    }
    
    @Override
    public List<Task> getTasksDueSoon() {
        return taskRepository.findTasksDueSoon();
    }
    
    @Override
    public List<Task> getUrgentTasks() {
        return taskRepository.findUrgentTasks();
    }
    
    @Override
    public TaskStatistics getTaskStatistics() {
        long totalTasks = taskRepository.count();
        long pendingTasks = taskRepository.countByStatus(Task.TaskStatus.PENDING);
        long inProgressTasks = taskRepository.countByStatus(Task.TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);
        long overdueTasks = taskRepository.findOverdueTasks().size();
        long urgentTasks = taskRepository.findUrgentTasks().size();
        
        return new TaskStatistics(totalTasks, pendingTasks, inProgressTasks, 
                                completedTasks, overdueTasks, urgentTasks);
    }
    
    @Override
    public boolean taskExists(UUID id) {
        return taskRepository.existsById(id);
    }
    
    @Override
    public boolean validateTaskData(String title, String description, Task.TaskPriority priority) {
        return title != null && !title.trim().isEmpty() &&
               priority != null;
    }
} 