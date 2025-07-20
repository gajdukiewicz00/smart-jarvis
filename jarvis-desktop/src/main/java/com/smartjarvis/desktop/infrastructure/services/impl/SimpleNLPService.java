package com.smartjarvis.desktop.infrastructure.services.impl;

import com.smartjarvis.desktop.domain.Command;
import com.smartjarvis.desktop.infrastructure.services.NLPService;

/**
 * Simple implementation of NLP Service
 */
public class SimpleNLPService implements NLPService {
    
    @Override
    public Command processIntent(String text) {
        return processIntent(text, null);
    }
    
    @Override
    public Command processIntent(String text, Object context) {
        // Simple intent recognition based on keywords
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("create") || lowerText.contains("add") || lowerText.contains("new")) {
            if (lowerText.contains("task")) {
                return new Command("Create Task", "Create a new task", Command.CommandType.TASK_CREATE, text);
            } else if (lowerText.contains("reminder")) {
                return new Command("Set Reminder", "Set a new reminder", Command.CommandType.REMINDER_SET, text);
            }
        } else if (lowerText.contains("list") || lowerText.contains("show") || lowerText.contains("get")) {
            if (lowerText.contains("task")) {
                return new Command("List Tasks", "List all tasks", Command.CommandType.TASK_LIST, text);
            }
        } else if (lowerText.contains("delete") || lowerText.contains("remove")) {
            if (lowerText.contains("task")) {
                return new Command("Delete Task", "Delete a task", Command.CommandType.TASK_DELETE, text);
            } else if (lowerText.contains("reminder")) {
                return new Command("Cancel Reminder", "Cancel a reminder", Command.CommandType.REMINDER_CANCEL, text);
            }
        } else if (lowerText.contains("system") || lowerText.contains("info")) {
            return new Command("System Info", "Get system information", Command.CommandType.SYSTEM_INFO, text);
        }
        
        // Default to custom action
        return new Command("Custom Action", "Execute custom action", Command.CommandType.CUSTOM_ACTION, text);
    }
} 