package com.smartjarvis.desktop.application.usecases;

import com.smartjarvis.desktop.domain.Reminder;
import com.smartjarvis.desktop.domain.repositories.ReminderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Use case for reminder management operations
 */
public class ReminderUseCase {
    private static final Logger logger = Logger.getLogger(ReminderUseCase.class.getName());
    
    private final ReminderRepository reminderRepository;
    
    public ReminderUseCase(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }
    
    /**
     * Set a new reminder
     * @param reminderData JSON string containing reminder data
     * @return result message
     */
    public String setReminder(String reminderData) {
        try {
            // Parse reminder data (simplified implementation)
            Reminder reminder = parseReminderFromJson(reminderData);
            
            // Save reminder
            Reminder savedReminder = reminderRepository.save(reminder);
            
            logger.info("Reminder set: " + savedReminder.getTitle());
            return "Reminder '" + savedReminder.getTitle() + "' set for " + savedReminder.getScheduledTime();
            
        } catch (Exception e) {
            logger.severe("Error setting reminder: " + e.getMessage());
            return "Error setting reminder: " + e.getMessage();
        }
    }
    
    /**
     * Cancel a reminder
     * @param reminderId reminder ID to cancel
     * @return result message
     */
    public String cancelReminder(String reminderId) {
        try {
            // This would parse the reminder ID from the input
            logger.info("Cancelling reminder: " + reminderId);
            
            // Placeholder implementation
            return "Reminder cancelled successfully";
            
        } catch (Exception e) {
            logger.severe("Error cancelling reminder: " + e.getMessage());
            return "Error cancelling reminder: " + e.getMessage();
        }
    }
    
    /**
     * Update a reminder
     * @param reminder the reminder to update
     * @return updated reminder
     */
    public Reminder updateReminder(Reminder reminder) {
        return reminderRepository.save(reminder);
    }
    
    /**
     * Get due reminders
     * @return list of due reminders
     */
    public List<Reminder> getDueReminders() {
        return reminderRepository.findDueReminders();
    }
    
    /**
     * Parse reminder from JSON string (simplified implementation)
     * @param jsonData JSON string containing reminder data
     * @return Reminder object
     */
    private Reminder parseReminderFromJson(String jsonData) {
        // Simplified parsing - in real app would use Jackson or Gson
        String title = "Default Reminder";
        String message = "Default Message";
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        Reminder.ReminderType type = Reminder.ReminderType.CUSTOM_REMINDER;
        
        // Extract basic info from JSON-like string
        if (jsonData.contains("title:")) {
            title = jsonData.split("title:")[1].split(",")[0].trim();
        }
        if (jsonData.contains("message:")) {
            message = jsonData.split("message:")[1].split(",")[0].trim();
        }
        if (jsonData.contains("scheduledTime:")) {
            String timeStr = jsonData.split("scheduledTime:")[1].split(",")[0].trim();
            scheduledTime = LocalDateTime.parse(timeStr);
        }
        if (jsonData.contains("type:")) {
            String typeStr = jsonData.split("type:")[1].split(",")[0].trim();
            type = Reminder.ReminderType.valueOf(typeStr.toUpperCase());
        }
        
        return new Reminder(title, message, scheduledTime, type);
    }
} 