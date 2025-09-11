package com.smartjarvis.calendar.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event reminder configuration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    /**
     * Reminder ID
     */
    private String id;

    /**
     * Minutes before event to trigger reminder
     */
    private int minutesBefore;

    /**
     * Reminder method
     */
    @Builder.Default
    private ReminderMethod method = ReminderMethod.NOTIFICATION;

    /**
     * Custom reminder message
     */
    private String message;

    /**
     * Whether reminder is active
     */
    @Builder.Default
    private boolean isActive = true;

    /**
     * When reminder was sent (null if not sent yet)
     */
    private LocalDateTime sentAt;

    /**
     * Whether reminder was acknowledged by user
     */
    @Builder.Default
    private boolean isAcknowledged = false;

    public enum ReminderMethod {
        NOTIFICATION("Уведомление"),
        VOICE("Голосовое"),
        EMAIL("Email"),
        SMS("SMS");

        private final String displayName;

        ReminderMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Check if reminder should be sent
     */
    public boolean shouldSend(LocalDateTime eventStartTime) {
        if (!isActive || sentAt != null) return false;
        
        LocalDateTime reminderTime = eventStartTime.minusMinutes(minutesBefore);
        LocalDateTime now = LocalDateTime.now();
        
        return now.isEqual(reminderTime) || now.isAfter(reminderTime);
    }

    /**
     * Mark reminder as sent
     */
    public void markAsSent() {
        this.sentAt = LocalDateTime.now();
    }
}
