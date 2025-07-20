package com.smartjarvis.desktop.infrastructure.services;

/**
 * Service interface for notifications
 */
public interface NotificationService {
    
    /**
     * Show a notification
     * @param title the notification title
     * @param message the notification message
     */
    void showNotification(String title, String message);
    
    /**
     * Show a notification with custom parameters
     * @param title the notification title
     * @param message the notification message
     * @param type the notification type
     * @param duration the notification duration in milliseconds
     */
    void showNotification(String title, String message, String type, long duration);
    
    /**
     * Clear all notifications
     */
    void clearNotifications();
} 