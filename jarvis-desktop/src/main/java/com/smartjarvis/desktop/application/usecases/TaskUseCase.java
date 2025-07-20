package com.smartjarvis.desktop.application.usecases;

import com.smartjarvis.desktop.domain.Task;
import com.smartjarvis.desktop.domain.repositories.TaskRepository;
import com.smartjarvis.desktop.infrastructure.services.TaskServiceClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Use case for task management operations
 */
public class TaskUseCase {
    private static final Logger logger = Logger.getLogger(TaskUseCase.class.getName());
    
    private final TaskRepository taskRepository;
    private final TaskServiceClient taskServiceClient;
    
    public TaskUseCase(TaskRepository taskRepository, TaskServiceClient taskServiceClient) {
        this.taskRepository = taskRepository;
        this.taskServiceClient = taskServiceClient;
    }
    
    /**
     * Create a new task
     * @param taskData JSON string containing task data
     * @return result message
     */
    public String createTask(String taskData) {
        try {
            // Parse task data (simplified - in real app would use JSON parser)
            Task task = parseTaskFromJson(taskData);
            
            // Save to local repository
            Task savedTask = taskRepository.save(task);
            
            // Sync with remote service
            taskServiceClient.createTask(savedTask);
            
            logger.info("Task created: " + savedTask.getTitle());
            return "Task '" + savedTask.getTitle() + "' created successfully";
            
        } catch (Exception e) {
            logger.severe("Error creating task: " + e.getMessage());
            return "Error creating task: " + e.getMessage();
        }
    }
    
    /**
     * Update an existing task
     * @param taskData JSON string containing task data
     * @return result message
     */
    public String updateTask(String taskData) {
        try {
            // Parse task data
            Task task = parseTaskFromJson(taskData);
            
            // Check if task exists
            Optional<Task> existingTask = taskRepository.findById(task.getId());
            if (existingTask.isEmpty()) {
                return "Task not found";
            }
            
            // Update task
            Task updatedTask = taskRepository.save(task);
            
            // Sync with remote service
            taskServiceClient.updateTask(updatedTask);
            
            logger.info("Task updated: " + updatedTask.getTitle());
            return "Task '" + updatedTask.getTitle() + "' updated successfully";
            
        } catch (Exception e) {
            logger.severe("Error updating task: " + e.getMessage());
            return "Error updating task: " + e.getMessage();
        }
    }
    
    /**
     * Delete a task
     * @param taskId task ID to delete
     * @return result message
     */
    public String deleteTask(String taskId) {
        try {
            UUID id = UUID.fromString(taskId);
            
            // Check if task exists
            Optional<Task> task = taskRepository.findById(id);
            if (task.isEmpty()) {
                return "Task not found";
            }
            
            // Delete from local repository
            taskRepository.deleteById(id);
            
            // Sync with remote service
            taskServiceClient.deleteTask(id);
            
            logger.info("Task deleted: " + task.get().getTitle());
            return "Task deleted successfully";
            
        } catch (Exception e) {
            logger.severe("Error deleting task: " + e.getMessage());
            return "Error deleting task: " + e.getMessage();
        }
    }
    
    /**
     * List all tasks
     * @return formatted task list
     */
    public String listTasks() {
        try {
            List<Task> tasks = taskRepository.findAll();
            
            if (tasks.isEmpty()) {
                return "No tasks found";
            }
            
            StringBuilder result = new StringBuilder("Tasks:\n");
            for (Task task : tasks) {
                result.append(String.format("- %s (%s) - %s\n", 
                    task.getTitle(), 
                    task.getStatus(), 
                    task.getDueDate() != null ? task.getDueDate().toString() : "No due date"));
            }
            
            return result.toString();
            
        } catch (Exception e) {
            logger.severe("Error listing tasks: " + e.getMessage());
            return "Error listing tasks: " + e.getMessage();
        }
    }
    
    /**
     * Get overdue tasks
     * @return list of overdue tasks
     */
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks();
    }
    
    /**
     * Get tasks due soon
     * @return list of tasks due soon
     */
    public List<Task> getTasksDueSoon() {
        return taskRepository.findTasksDueSoon();
    }
    
    /**
     * Complete a task
     * @param taskId task ID to complete
     * @return result message
     */
    public String completeTask(String taskId) {
        try {
            UUID id = UUID.fromString(taskId);
            
            Optional<Task> taskOpt = taskRepository.findById(id);
            if (taskOpt.isEmpty()) {
                return "Task not found";
            }
            
            Task task = taskOpt.get();
            task.complete();
            taskRepository.save(task);
            
            // Sync with remote service
            taskServiceClient.updateTask(task);
            
            logger.info("Task completed: " + task.getTitle());
            return "Task '" + task.getTitle() + "' completed successfully";
            
        } catch (Exception e) {
            logger.severe("Error completing task: " + e.getMessage());
            return "Error completing task: " + e.getMessage();
        }
    }
    
    /**
     * Get task statistics
     * @return task statistics
     */
    public String getTaskStatistics() {
        try {
            long totalTasks = taskRepository.count();
            long pendingTasks = taskRepository.countByStatus(Task.TaskStatus.PENDING);
            long completedTasks = taskRepository.countByStatus(Task.TaskStatus.COMPLETED);
            long overdueTasks = taskRepository.findOverdueTasks().size();
            
            return String.format("Task Statistics:\n" +
                    "Total: %d\n" +
                    "Pending: %d\n" +
                    "Completed: %d\n" +
                    "Overdue: %d",
                    totalTasks, pendingTasks, completedTasks, overdueTasks);
                    
        } catch (Exception e) {
            logger.severe("Error getting task statistics: " + e.getMessage());
            return "Error getting task statistics: " + e.getMessage();
        }
    }
    
    /**
     * Parse task from JSON string (simplified implementation)
     * @param jsonData JSON string containing task data
     * @return Task object
     */
    private Task parseTaskFromJson(String jsonData) {
        // Simplified parsing - in real app would use Jackson or Gson
        // This is a placeholder implementation
        String title = "Default Task";
        String description = "Default Description";
        Task.TaskPriority priority = Task.TaskPriority.MEDIUM;
        LocalDateTime dueDate = null;
        
        // Extract basic info from JSON-like string
        if (jsonData.contains("title:")) {
            title = jsonData.split("title:")[1].split(",")[0].trim();
        }
        if (jsonData.contains("description:")) {
            description = jsonData.split("description:")[1].split(",")[0].trim();
        }
        if (jsonData.contains("priority:")) {
            String priorityStr = jsonData.split("priority:")[1].split(",")[0].trim();
            priority = Task.TaskPriority.valueOf(priorityStr.toUpperCase());
        }
        if (jsonData.contains("dueDate:")) {
            String dateStr = jsonData.split("dueDate:")[1].split(",")[0].trim();
            dueDate = LocalDateTime.parse(dateStr);
        }
        
        return new Task(title, description, priority, dueDate);
    }
} 