package com.smartjarvis.todo.kafka;

import com.smartjarvis.events.DMDecisionEvent;
import com.smartjarvis.todo.dto.CreateTodoRequest;
import com.smartjarvis.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Kafka listener for DM decision events
 * Processes todo-related decisions from Dialog Manager
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DMDecisionListener {

    private final TodoService todoService;

    @KafkaListener(topics = "dm.decision", groupId = "todo-service")
    public void handleDecision(DMDecisionEvent decisionEvent) {
        try {
            // Only process decisions for todo-service
            if (!"todo-service".equals(decisionEvent.getTargetService())) {
                return;
            }

            log.info("Processing todo decision: sessionId={}, action={}", 
                    decisionEvent.getSessionId(), decisionEvent.getAction());

            switch (decisionEvent.getAction()) {
                case "create_task" -> handleCreateTask(decisionEvent);
                case "list_tasks" -> handleListTasks(decisionEvent);
                case "complete_task" -> handleCompleteTask(decisionEvent);
                default -> log.warn("Unknown todo action: {}", decisionEvent.getAction());
            }

        } catch (Exception e) {
            log.error("Failed to process todo decision: sessionId={}", 
                    decisionEvent.getSessionId(), e);
        }
    }

    /**
     * Handle task creation
     */
    private void handleCreateTask(DMDecisionEvent decision) {
        String title = decision.getParameters().get("title");
        if (title == null || title.trim().isEmpty()) {
            title = decision.getParameters().get("task");
        }
        
        if (title == null || title.trim().isEmpty()) {
            log.warn("No task title provided in decision: sessionId={}", decision.getSessionId());
            return;
        }

        CreateTodoRequest request = new CreateTodoRequest();
        request.setUserId(decision.getUserId());
        request.setTitle(title.trim());
        request.setDescription("Создано голосовой командой");

        // Parse due date if provided
        String dueDateStr = decision.getParameters().get("due_date");
        String dueTimeStr = decision.getParameters().get("due_time");
        
        if (dueDateStr != null || dueTimeStr != null) {
            LocalDateTime dueDate = parseDueDateTime(dueDateStr, dueTimeStr);
            request.setDueDate(dueDate);
        }

        try {
            todoService.createTodo(request);
            log.info("Task created via voice command: title='{}', userId='{}'", 
                    title, decision.getUserId());
        } catch (Exception e) {
            log.error("Failed to create task via voice command: title='{}'", title, e);
        }
    }

    /**
     * Handle task listing
     */
    private void handleListTasks(DMDecisionEvent decision) {
        try {
            var todos = todoService.getPendingTodos(decision.getUserId());
            log.info("Listed {} pending tasks for user: '{}'", 
                    todos.size(), decision.getUserId());
            
            // TODO: Send response back through TTS
            
        } catch (Exception e) {
            log.error("Failed to list tasks for user: '{}'", decision.getUserId(), e);
        }
    }

    /**
     * Handle task completion
     */
    private void handleCompleteTask(DMDecisionEvent decision) {
        String taskIdStr = decision.getParameters().get("task_id");
        if (taskIdStr == null) {
            log.warn("No task ID provided for completion: sessionId={}", decision.getSessionId());
            return;
        }

        try {
            todoService.markCompleted(taskIdStr, decision.getUserId());
            log.info("Task completed via voice command: id='{}', userId='{}'", 
                    taskIdStr, decision.getUserId());
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
