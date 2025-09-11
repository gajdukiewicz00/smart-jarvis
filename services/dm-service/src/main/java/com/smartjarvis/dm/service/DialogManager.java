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

        // Home control - расширенные действия
        map.put("home_light_control", ResponseAction.builder()
            .action("control_light")
            .targetService("home-bridge")
            .responseText("Управляю освещением")
            .requiresConfirmation(false)
            .build());
            
        map.put("home_media_control", ResponseAction.builder()
            .action("control_media")
            .targetService("home-bridge")
            .responseText("Управляю медиа")
            .requiresConfirmation(false)
            .build());
            
        map.put("home_scene", ResponseAction.builder()
            .action("activate_scene")
            .targetService("home-bridge")
            .responseText("Активирую сцену")
            .requiresConfirmation(false)
            .build());
            
        // Legacy home control (fallback)
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

        // Device control commands
        map.put("device_volume_up", ResponseAction.builder()
            .action("set_volume")
            .targetService("device-agent")
            .responseText("Делаю громче")
            .requiresConfirmation(false)
            .build());
            
        map.put("device_volume_down", ResponseAction.builder()
            .action("set_volume")
            .targetService("device-agent")
            .responseText("Делаю тише")
            .requiresConfirmation(false)
            .build());
            
        map.put("device_volume_set", ResponseAction.builder()
            .action("set_volume")
            .targetService("device-agent")
            .responseText("Устанавливаю громкость")
            .requiresConfirmation(false)
            .build());
            
        map.put("device_open_app", ResponseAction.builder()
            .action("open_app")
            .targetService("device-agent")
            .responseText("Открываю приложение")
            .requiresConfirmation(false)
            .build());
            
        map.put("device_open_url", ResponseAction.builder()
            .action("open_url")
            .targetService("device-agent")
            .responseText("Открываю ссылку")
            .requiresConfirmation(false)
            .build());
            
        map.put("device_screenshot", ResponseAction.builder()
            .action("take_screenshot")
            .targetService("device-agent")
            .responseText("Делаю скриншот")
            .requiresConfirmation(false)
            .build());
            
        map.put("device_lock", ResponseAction.builder()
            .action("lock_screen")
            .targetService("device-agent")
            .responseText("Блокирую экран")
            .requiresConfirmation(true)  // Требует подтверждения
            .build());
            
        map.put("device_media", ResponseAction.builder()
            .action("media_control")
            .targetService("device-agent")
            .responseText("Управляю воспроизведением")
            .requiresConfirmation(false)
            .build());

        // Money/Finance commands
        map.put("money_expense", ResponseAction.builder()
            .action("add_expense")
            .targetService("money-service")
            .responseText("Записываю расход")
            .requiresConfirmation(false)
            .build());
            
        map.put("money_income", ResponseAction.builder()
            .action("add_income")
            .targetService("money-service")
            .responseText("Записываю доход")
            .requiresConfirmation(false)
            .build());
            
        map.put("money_balance", ResponseAction.builder()
            .action("get_balance")
            .targetService("money-service")
            .responseText("Проверяю баланс")
            .requiresConfirmation(false)
            .build());
            
        map.put("money_expense_report", ResponseAction.builder()
            .action("get_expense_report")
            .targetService("money-service")
            .responseText("Формирую отчет по расходам")
            .requiresConfirmation(false)
            .build());
            
        map.put("money_income_report", ResponseAction.builder()
            .action("get_income_report")
            .targetService("money-service")
            .responseText("Формирую отчет по доходам")
            .requiresConfirmation(false)
            .build());
            
        map.put("money_stats", ResponseAction.builder()
            .action("get_financial_stats")
            .targetService("money-service")
            .responseText("Показываю финансовую статистику")
            .requiresConfirmation(false)
            .build());
            
        map.put("money_category_spending", ResponseAction.builder()
            .action("get_category_spending")
            .targetService("money-service")
            .responseText("Показываю расходы по категориям")
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
            case "device_volume_up", "device_volume_down", "device_volume_set" -> {
                if (entities.containsKey("volume")) {
                    parameters.put("volume", entities.get("volume"));
                }
                if (entities.containsKey("volume_action")) {
                    parameters.put("volume_action", entities.get("volume_action"));
                }
            }
            case "device_open_app" -> {
                if (entities.containsKey("app_name")) {
                    parameters.put("app_name", entities.get("app_name"));
                }
            }
            case "device_open_url" -> {
                if (entities.containsKey("url")) {
                    parameters.put("url", entities.get("url"));
                }
            }
            case "device_media" -> {
                if (entities.containsKey("media_action")) {
                    parameters.put("media_action", entities.get("media_action"));
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
