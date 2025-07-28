package com.smartjarvis.taskservice.service;

import com.smartjarvis.taskservice.domain.Task;
import com.smartjarvis.taskservice.dto.TaskDto;
import com.smartjarvis.taskservice.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    
    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    // Create task
    public TaskDto createTask(TaskDto taskDto) {
        logger.info("Creating task: {}", taskDto.getTitle());
        try {
            Task task = taskDto.toEntity();
            Task savedTask = taskRepository.save(task);
            logger.info("Task created successfully with ID: {}", savedTask.getId());
            return new TaskDto(savedTask);
        } catch (Exception e) {
            logger.error("Error creating task: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // Get task by ID
    public Optional<TaskDto> getTaskById(UUID id) {
        logger.info("Getting task by ID: {}", id);
        try {
            Optional<Task> task = taskRepository.findById(id);
            if (task.isPresent()) {
                logger.info("Task found with ID: {}", id);
                return task.map(TaskDto::new);
            } else {
                logger.warn("Task not found with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error getting task by ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    // Get all tasks
    public List<TaskDto> getAllTasks() {
        logger.info("Getting all tasks from database");
        try {
            List<Task> tasks = taskRepository.findAll();
            logger.info("Found {} tasks in database", tasks.size());
            
            List<TaskDto> taskDtos = tasks.stream()
                    .map(task -> {
                        try {
                            TaskDto dto = new TaskDto(task);
                            logger.debug("Converted task: {} -> {}", task.getId(), dto.getTitle());
                            return dto;
                        } catch (Exception e) {
                            logger.error("Error converting task {} to DTO: {}", task.getId(), e.getMessage(), e);
                            throw e;
                        }
                    })
                    .collect(Collectors.toList());
            
            logger.info("Successfully converted {} tasks to DTOs", taskDtos.size());
            return taskDtos;
        } catch (Exception e) {
            logger.error("Error getting all tasks: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // Update task
    public Optional<TaskDto> updateTask(UUID id, TaskDto taskDto) {
        logger.info("Updating task with ID: {}", id);
        try {
            Optional<Task> existingTask = taskRepository.findById(id);
            if (existingTask.isPresent()) {
                Task task = existingTask.get();
                task.setTitle(taskDto.getTitle());
                task.setDescription(taskDto.getDescription());
                task.setPriority(taskDto.getPriority());
                task.setStatus(taskDto.getStatus());
                task.setCategory(taskDto.getCategory());
                task.setDueDate(taskDto.getDueDate());
                
                Task updatedTask = taskRepository.save(task);
                logger.info("Task updated successfully with ID: {}", id);
                return Optional.of(new TaskDto(updatedTask));
            } else {
                logger.warn("Task not found for update with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error updating task with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    // Delete task
    public boolean deleteTask(UUID id) {
        logger.info("Deleting task with ID: {}", id);
        try {
            if (taskRepository.existsById(id)) {
                taskRepository.deleteById(id);
                logger.info("Task deleted successfully with ID: {}", id);
                return true;
            } else {
                logger.warn("Task not found for deletion with ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting task with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    // Complete task
    public Optional<TaskDto> completeTask(UUID id) {
        logger.info("Completing task with ID: {}", id);
        try {
            Optional<Task> task = taskRepository.findById(id);
            if (task.isPresent()) {
                Task t = task.get();
                t.complete();
                Task completedTask = taskRepository.save(t);
                logger.info("Task completed successfully with ID: {}", id);
                return Optional.of(new TaskDto(completedTask));
            } else {
                logger.warn("Task not found for completion with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error completing task with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    // Start task
    public Optional<TaskDto> startTask(UUID id) {
        logger.info("Starting task with ID: {}", id);
        try {
            Optional<Task> task = taskRepository.findById(id);
            if (task.isPresent()) {
                Task t = task.get();
                t.start();
                Task startedTask = taskRepository.save(t);
                logger.info("Task started successfully with ID: {}", id);
                return Optional.of(new TaskDto(startedTask));
            } else {
                logger.warn("Task not found for starting with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error starting task with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    // Pause task
    public Optional<TaskDto> pauseTask(UUID id) {
        logger.info("Pausing task with ID: {}", id);
        try {
            Optional<Task> task = taskRepository.findById(id);
            if (task.isPresent()) {
                Task t = task.get();
                t.pause();
                Task pausedTask = taskRepository.save(t);
                logger.info("Task paused successfully with ID: {}", id);
                return Optional.of(new TaskDto(pausedTask));
            } else {
                logger.warn("Task not found for pausing with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error pausing task with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    // Get tasks by status
    public List<TaskDto> getTasksByStatus(Task.TaskStatus status) {
        logger.info("Getting tasks by status: {}", status);
        try {
            List<Task> tasks = taskRepository.findByStatus(status);
            List<TaskDto> taskDtos = tasks.stream()
                    .map(TaskDto::new)
                    .collect(Collectors.toList());
            logger.info("Found {} tasks with status: {}", taskDtos.size(), status);
            return taskDtos;
        } catch (Exception e) {
            logger.error("Error getting tasks by status {}: {}", status, e.getMessage(), e);
            throw e;
        }
    }
    
    // Get tasks by priority
    public List<TaskDto> getTasksByPriority(Task.TaskPriority priority) {
        logger.info("Getting tasks by priority: {}", priority);
        try {
            List<Task> tasks = taskRepository.findByPriority(priority);
            List<TaskDto> taskDtos = tasks.stream()
                    .map(TaskDto::new)
                    .collect(Collectors.toList());
            logger.info("Found {} tasks with priority: {}", taskDtos.size(), priority);
            return taskDtos;
        } catch (Exception e) {
            logger.error("Error getting tasks by priority {}: {}", priority, e.getMessage(), e);
            throw e;
        }
    }
    
    // Get tasks by category
    public List<TaskDto> getTasksByCategory(String category) {
        logger.info("Getting tasks by category: {}", category);
        try {
            List<Task> tasks = taskRepository.findByCategory(category);
            List<TaskDto> taskDtos = tasks.stream()
                    .map(TaskDto::new)
                    .collect(Collectors.toList());
            logger.info("Found {} tasks with category: {}", taskDtos.size(), category);
            return taskDtos;
        } catch (Exception e) {
            logger.error("Error getting tasks by category {}: {}", category, e.getMessage(), e);
            throw e;
        }
    }
    
    // Get overdue tasks
    public List<TaskDto> getOverdueTasks() {
        logger.info("Getting overdue tasks");
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Task> tasks = taskRepository.findOverdueTasks(now);
            List<TaskDto> taskDtos = tasks.stream()
                    .map(TaskDto::new)
                    .collect(Collectors.toList());
            logger.info("Found {} overdue tasks", taskDtos.size());
            return taskDtos;
        } catch (Exception e) {
            logger.error("Error getting overdue tasks: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // Get tasks due soon
    public List<TaskDto> getTasksDueSoon() {
        logger.info("Getting tasks due soon");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime soon = now.plusDays(1);
            List<Task> tasks = taskRepository.findTasksDueSoon(now, soon);
            List<TaskDto> taskDtos = tasks.stream()
                    .map(TaskDto::new)
                    .collect(Collectors.toList());
            logger.info("Found {} tasks due soon", taskDtos.size());
            return taskDtos;
        } catch (Exception e) {
            logger.error("Error getting tasks due soon: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // Search tasks by title
    public List<TaskDto> searchTasksByTitle(String title) {
        logger.info("Searching tasks by title: {}", title);
        try {
            List<Task> tasks = taskRepository.findByTitleContainingIgnoreCase(title);
            List<TaskDto> taskDtos = tasks.stream()
                    .map(TaskDto::new)
                    .collect(Collectors.toList());
            logger.info("Found {} tasks with title containing: {}", taskDtos.size(), title);
            return taskDtos;
        } catch (Exception e) {
            logger.error("Error searching tasks by title {}: {}", title, e.getMessage(), e);
            throw e;
        }
    }
    
    // Search tasks by description
    public List<TaskDto> searchTasksByDescription(String description) {
        logger.info("Searching tasks by description: {}", description);
        try {
            List<Task> tasks = taskRepository.findByDescriptionContainingIgnoreCase(description);
            List<TaskDto> taskDtos = tasks.stream()
                    .map(TaskDto::new)
                    .collect(Collectors.toList());
            logger.info("Found {} tasks with description containing: {}", taskDtos.size(), description);
            return taskDtos;
        } catch (Exception e) {
            logger.error("Error searching tasks by description {}: {}", description, e.getMessage(), e);
            throw e;
        }
    }
    
    // Get task statistics
    public TaskStatistics getTaskStatistics() {
        logger.info("Getting task statistics");
        try {
            long totalTasks = taskRepository.count();
            long pendingTasks = taskRepository.countByStatus(Task.TaskStatus.PENDING);
            long inProgressTasks = taskRepository.countByStatus(Task.TaskStatus.IN_PROGRESS);
            long completedTasks = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);
            long overdueTasks = taskRepository.findOverdueTasks(LocalDateTime.now()).size();
            
            TaskStatistics statistics = new TaskStatistics(totalTasks, pendingTasks, inProgressTasks, completedTasks, overdueTasks);
            logger.info("Retrieved task statistics: total={}, pending={}, inProgress={}, completed={}, overdue={}", 
                       totalTasks, pendingTasks, inProgressTasks, completedTasks, overdueTasks);
            return statistics;
        } catch (Exception e) {
            logger.error("Error getting task statistics: {}", e.getMessage(), e);
            throw e;
        }
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