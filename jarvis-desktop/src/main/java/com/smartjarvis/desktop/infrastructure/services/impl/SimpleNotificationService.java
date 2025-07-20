package com.smartjarvis.desktop.infrastructure.services.impl;

import com.smartjarvis.desktop.infrastructure.services.NotificationService;

/**
 * Simple implementation of Notification Service
 */
public class SimpleNotificationService implements NotificationService {
    
    @Override
    public void showNotification(String title, String message) {
        showNotification(title, message, "INFO", 5000);
    }
    
    @Override
    public void showNotification(String title, String message, String type, long duration) {
        // Placeholder implementation
        // In a real implementation, this would show a system notification
        System.out.println("[" + type + "] " + title + ": " + message);
    }
    
    @Override
    public void clearNotifications() {
        // Placeholder implementation
        // In a real implementation, this would clear all notifications
        System.out.println("All notifications cleared");
    }
} 