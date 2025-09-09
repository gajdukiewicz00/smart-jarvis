package com.smartjarvis.dm.service;

import com.smartjarvis.dm.model.DialogDecision;
import com.smartjarvis.dm.model.ResponseAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Dialog Manager - makes decisions based on recognized intents
 * Uses decision table to map intents to actions and target services
 */
@Service
@Slf4j
public class DialogManager {

    // Decision table: intent -> action
    private final Map<String, ResponseAction> actionMap;
    
    public DialogManager() {
        this.actionMap = initializeActionMap();
        log.info("Dialog Manager initialized with {} action mappings", actionMap.size());
    }

    /**
     * Process intent and make decision about action to take
     */
    public DialogDecision processIntent(String intent, Map<String, String> entities, 
                                      String sessionId, String userId) {
        
        log.info("Processing intent: '{}' with {} entities for session: {}", 
                intent, entities.size(), sessionId);

        // Get action from decision table
        ResponseAction action = actionMap.getOrDefault(intent, getDefaultAction());
        
        // Create decision with context
        DialogDecision decision = DialogDecision.builder()
            .sessionId(sessionId)
            .userId(userId)
            .action(action.getAction())
            .targetService(action.getTargetService())
            .responseText(action.getResponseText())
            .parameters(buildParameters(intent, entities))
            .requiresConfirmation(action.isRequiresConfirmation())
            .timestamp(System.currentTimeMillis())
            .build();

        log.info("Decision made: action='{}', target='{}', confirmation={}", 
                action.getAction(), action.getTargetService(), action.isRequiresConfirmation());

        return decision;
    }

    /**
     * Initialize decision table with intent -> action mappings
     */
    private Map<String, ResponseAction> initializeActionMap() {
        Map<String, ResponseAction> map = new HashMap<>();

        // Greeting responses
        map.put("greeting", ResponseAction.builder()
            .action("respond")
            .targetService("tts-service")
            .responseText("К вашим услугам")
            .requiresConfirmation(false)
            .build());

        // Help responses  
        map.put("help", ResponseAction.builder()
            .action("respond")
            .targetService("tts-service")
            .responseText("Я умею управлять задачами, умным домом и отвечать на вопросы")
            .requiresConfirmation(false)
            .build());

        // Todo operations - расширенные действия
        map.put("todo_create", ResponseAction.builder()
            .action("create_task")
            .targetService("todo-service")
            .responseText("Создаю задачу")
            .requiresConfirmation(false)
            .build());

        map.put("todo_list", ResponseAction.builder()
            .action("list_tasks")
            .targetService("todo-service")
            .responseText("Показываю список задач")
            .requiresConfirmation(false)
            .build());
            
        map.put("todo_complete", ResponseAction.builder()
            .action("complete_task")
            .targetService("todo-service")
            .responseText("Отмечаю задачу выполненной")
            .requiresConfirmation(false)
            .build());
            
        map.put("todo_delete", ResponseAction.builder()
            .action("delete_task")
            .targetService("todo-service")
            .responseText("Удаляю задачу")
            .requiresConfirmation(true)  // Требует подтверждения
            .build());
            
        map.put("todo_stats", ResponseAction.builder()
            .action("get_stats")
            .targetService("todo-service")
            .responseText("Показываю статистику задач")
            .requiresConfirmation(false)
            .build());

        // Home control
        map.put("home_control", ResponseAction.builder()
            .action("control_device")
            .targetService("home-bridge")
            .responseText("Выполняю команду")
            .requiresConfirmation(false)
            .build());

        // Time queries
        map.put("time_query", ResponseAction.builder()
            .action("get_time")
            .targetService("system-service")
            .responseText("Сейчас")
            .requiresConfirmation(false)
            .build());

        // Weather queries
        map.put("weather_query", ResponseAction.builder()
            .action("get_weather")
            .targetService("weather-service")
            .responseText("Проверяю погоду")
            .requiresConfirmation(false)
            .build());

        // Stop command
        map.put("stop", ResponseAction.builder()
            .action("stop_all")
            .targetService("voice-gateway")
            .responseText("Останавливаю")
            .requiresConfirmation(false)
            .build());

        return map;
    }

    /**
     * Build parameters for action based on intent and entities
     */
    private Map<String, String> buildParameters(String intent, Map<String, String> entities) {
        Map<String, String> parameters = new HashMap<>(entities);
        parameters.put("intent", intent);
        
        // Add intent-specific parameters
        switch (intent) {
            case "todo_create" -> {
                if (entities.containsKey("task")) {
                    parameters.put("title", entities.get("task"));
                }
                if (entities.containsKey("time")) {
                    parameters.put("due_time", entities.get("time"));
                }
                if (entities.containsKey("date")) {
                    parameters.put("due_date", entities.get("date"));
                }
            }
            case "home_control" -> {
                if (entities.containsKey("device")) {
                    parameters.put("device_type", entities.get("device"));
                }
                if (entities.containsKey("action")) {
                    parameters.put("device_action", entities.get("action"));
                }
                if (entities.containsKey("room")) {
                    parameters.put("location", entities.get("room"));
                }
                if (entities.containsKey("number")) {
                    parameters.put("value", entities.get("number"));
                }
            }
        }

        return parameters;
    }

    /**
     * Get default action for unknown intents
     */
    private ResponseAction getDefaultAction() {
        return ResponseAction.builder()
            .action("respond")
            .targetService("tts-service")
            .responseText("Не понял команду. Скажите 'помощь' для списка доступных команд")
            .requiresConfirmation(false)
            .build();
    }

    /**
     * Check if intent requires confirmation
     */
    public boolean requiresConfirmation(String intent) {
        ResponseAction action = actionMap.get(intent);
        return action != null && action.isRequiresConfirmation();
    }

    /**
     * Get supported intents
     */
    public String[] getSupportedIntents() {
        return actionMap.keySet().toArray(new String[0]);
    }
}
