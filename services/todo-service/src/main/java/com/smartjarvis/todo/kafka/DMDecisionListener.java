package com.smartjarvis.todo.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartjarvis.todo.dto.CreateTodoRequest;
import com.smartjarvis.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka listener for DM decision events
 * Processes todo-related decisions from Dialog Manager
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DMDecisionListener {

    private final TodoService todoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "dm.decision", groupId = "todo-service")
    public void handleDecision(String message) {
        try {
            Map<?,?> decisionEvent = objectMapper.readValue(message, Map.class);
            // Only process decisions for todo-service
            if (!"todo-service".equals(decisionEvent.get("targetService"))) {
                return;
            }

            String sessionId = (String) decisionEvent.get("sessionId");
            String userId = (String) decisionEvent.get("userId");
            String action = (String) decisionEvent.get("action");
            @SuppressWarnings("unchecked")
            Map<String,String> parameters = (Map<String,String>) decisionEvent.get("parameters");

            log.info("Processing todo decision: sessionId={}, action={}", sessionId, action);

            switch (action) {
                case "create_task" -> handleCreateTask(userId, sessionId, parameters);
                case "list_tasks" -> handleListTasks(userId, sessionId);
                case "complete_task" -> handleCompleteTask(userId, sessionId, parameters);
                default -> log.warn("Unknown todo action: {}", action);
            }

        } catch (Exception e) {
            log.error("Failed to process todo decision message", e);
        }
    }

    /**
     * Handle task creation
     */
    private void handleCreateTask(String userId, String sessionId, Map<String,String> parameters) {
        String title = parameters.get("title");
        if (title == null || title.trim().isEmpty()) {
            title = parameters.get("task");
        }
        
        if (title == null || title.trim().isEmpty()) {
            log.warn("No task title provided in decision: sessionId={}", sessionId);
            return;
        }

        CreateTodoRequest request = new CreateTodoRequest();
        request.setUserId(userId);
        request.setTitle(title.trim());
        request.setDescription("Создано голосовой командой");

        // Parse due date if provided
        String dueDateStr = parameters.get("due_date");
        String dueTimeStr = parameters.get("due_time");
        
        if (dueDateStr != null || dueTimeStr != null) {
            LocalDateTime dueDate = parseDueDateTime(dueDateStr, dueTimeStr);
            request.setDueDate(dueDate);
        }

        try {
            todoService.createTodo(request);
            log.info("Task created via voice command: title='{}', userId='{}'", 
                    title, userId);
        } catch (Exception e) {
            log.error("Failed to create task via voice command: title='{}'", title, e);
        }
    }

    /**
     * Handle task listing
     */
    private void handleListTasks(String userId, String sessionId) {
        try {
            var todos = todoService.getPendingTodos(userId);
            log.info("Listed {} pending tasks for user: '{}'", 
                    todos.size(), userId);
            
            // TODO: Send response back through TTS
            
        } catch (Exception e) {
            log.error("Failed to list tasks for user: '{}'", userId, e);
        }
    }

    /**
     * Handle task completion
     */
    private void handleCompleteTask(String userId, String sessionId, Map<String,String> parameters) {
        String taskIdStr = parameters.get("task_id");
        if (taskIdStr == null) {
            log.warn("No task ID provided for completion: sessionId={}", sessionId);
            return;
        }

        try {
            todoService.markCompleted(taskIdStr, userId);
            log.info("Task completed via voice command: id='{}', userId='{}'", 
                    taskIdStr, userId);
        } catch (Exception e) {
            log.error("Failed to complete task via voice command: id='{}'", taskIdStr, e);
        }
    }

    /**
     * Parse due date and time from strings
     */
    private LocalDateTime parseDueDateTime(String dateStr, String timeStr) {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Handle relative dates
            if ("завтра".equals(dateStr)) {
                LocalDateTime tomorrow = now.plusDays(1);
                
                if (timeStr != null) {
                    // Try to parse time
                    try {
                        String[] timeParts = timeStr.split("[:\\.]");
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;
                        
                        return tomorrow.withHour(hour).withMinute(minute).withSecond(0);
                    } catch (Exception e) {
                        log.warn("Failed to parse time: '{}'", timeStr);
                        return tomorrow.withHour(9).withMinute(0).withSecond(0); // Default 9 AM
                    }
                } else {
                    return tomorrow.withHour(9).withMinute(0).withSecond(0); // Default 9 AM
                }
            } else if ("сегодня".equals(dateStr)) {
                if (timeStr != null) {
                    // Parse time for today
                    try {
                        String[] timeParts = timeStr.split("[:\\.]");
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = timeParts.length > 1 ? Integer.parseInt(timeParts[1]) : 0;
                        
                        return now.withHour(hour).withMinute(minute).withSecond(0);
                    } catch (Exception e) {
                        return now.plusHours(1); // Default 1 hour from now
                    }
                } else {
                    return now.plusHours(1); // Default 1 hour from now
                }
            }
            
            // If no relative date, return default
            return now.plusDays(1).withHour(9).withMinute(0).withSecond(0);
            
        } catch (Exception e) {
            log.warn("Failed to parse due date/time: date='{}', time='{}'", dateStr, timeStr);
            return LocalDateTime.now().plusDays(1);
        }
    }
}
