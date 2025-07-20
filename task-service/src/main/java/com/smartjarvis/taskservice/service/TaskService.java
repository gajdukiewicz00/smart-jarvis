package com.smartjarvis.taskservice.service;

import com.smartjarvis.taskservice.domain.Task;
import com.smartjarvis.taskservice.dto.TaskDto;
import com.smartjarvis.taskservice.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {
    
    private final TaskRepository taskRepository;
    
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    // Create task
    public TaskDto createTask(TaskDto taskDto) {
        Task task = taskDto.toEntity();
        Task savedTask = taskRepository.save(task);
        return new TaskDto(savedTask);
    }
    
    // Get task by ID
    public Optional<TaskDto> getTaskById(UUID id) {
        return taskRepository.findById(id)
                .map(TaskDto::new);
    }
    
    // Get all tasks
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Update task
    public Optional<TaskDto> updateTask(UUID id, TaskDto taskDto) {
        return taskRepository.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(taskDto.getTitle());
                    existingTask.setDescription(taskDto.getDescription());
                    existingTask.setPriority(taskDto.getPriority());
                    existingTask.setStatus(taskDto.getStatus());
                    existingTask.setCategory(taskDto.getCategory());
                    existingTask.setDueDate(taskDto.getDueDate());
                    
                    Task updatedTask = taskRepository.save(existingTask);
                    return new TaskDto(updatedTask);
                });
    }
    
    // Delete task
    public boolean deleteTask(UUID id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Complete task
    public Optional<TaskDto> completeTask(UUID id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.complete();
                    Task completedTask = taskRepository.save(task);
                    return new TaskDto(completedTask);
                });
    }
    
    // Start task
    public Optional<TaskDto> startTask(UUID id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.start();
                    Task startedTask = taskRepository.save(task);
                    return new TaskDto(startedTask);
                });
    }
    
    // Pause task
    public Optional<TaskDto> pauseTask(UUID id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.pause();
                    Task pausedTask = taskRepository.save(task);
                    return new TaskDto(pausedTask);
                });
    }
    
    // Get tasks by status
    public List<TaskDto> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Get tasks by priority
    public List<TaskDto> getTasksByPriority(Task.TaskPriority priority) {
        return taskRepository.findByPriority(priority).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Get tasks by category
    public List<TaskDto> getTasksByCategory(String category) {
        return taskRepository.findByCategory(category).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Get overdue tasks
    public List<TaskDto> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findOverdueTasks(now).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Get tasks due soon
    public List<TaskDto> getTasksDueSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soon = now.plusDays(1);
        return taskRepository.findTasksDueSoon(now, soon).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Search tasks by title
    public List<TaskDto> searchTasksByTitle(String title) {
        return taskRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Search tasks by description
    public List<TaskDto> searchTasksByDescription(String description) {
        return taskRepository.findByDescriptionContainingIgnoreCase(description).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Get task statistics
    public TaskStatistics getTaskStatistics() {
        long totalTasks = taskRepository.count();
        long pendingTasks = taskRepository.countByStatus(Task.TaskStatus.PENDING);
        long inProgressTasks = taskRepository.countByStatus(Task.TaskStatus.IN_PROGRESS);
        long completedTasks = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);
        long overdueTasks = taskRepository.findOverdueTasks(LocalDateTime.now()).size();
        
        return new TaskStatistics(totalTasks, pendingTasks, inProgressTasks, completedTasks, overdueTasks);
    }
    
    // Statistics class
    public static class TaskStatistics {
        private final long totalTasks;
        private final long pendingTasks;
        private final long inProgressTasks;
        private final long completedTasks;
        private final long overdueTasks;
        
        public TaskStatistics(long totalTasks, long pendingTasks, long inProgressTasks, 
                           long completedTasks, long overdueTasks) {
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.inProgressTasks = inProgressTasks;
            this.completedTasks = completedTasks;
            this.overdueTasks = overdueTasks;
        }
        
        // Getters
        public long getTotalTasks() { return totalTasks; }
        public long getPendingTasks() { return pendingTasks; }
        public long getInProgressTasks() { return inProgressTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getOverdueTasks() { return overdueTasks; }
    }
} 