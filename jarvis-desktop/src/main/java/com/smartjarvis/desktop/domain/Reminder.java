package com.smartjarvis.desktop.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a reminder in the JARVIS system
 */
public class Reminder {
    private final UUID id;
    private String title;
    private String message;
    private LocalDateTime scheduledTime;
    private ReminderStatus status;
    private ReminderType type;
    private final LocalDateTime createdAt;
    private LocalDateTime triggeredAt;
    private boolean recurring;
    private String recurrencePattern; // Cron-like pattern for recurring reminders

    public Reminder(String title, String message, LocalDateTime scheduledTime, ReminderType type) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.message = message;
        this.scheduledTime = scheduledTime;
        this.type = type;
        this.status = ReminderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.recurring = false;
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public ReminderStatus getStatus() { return status; }
    public ReminderType getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public boolean isRecurring() { return recurring; }
    public String getRecurrencePattern() { return recurrencePattern; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    public void setType(ReminderType type) { this.type = type; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }
    public void setRecurrencePattern(String recurrencePattern) { this.recurrencePattern = recurrencePattern; }

    // Business methods
    public void trigger() {
        this.status = ReminderStatus.TRIGGERED;
        this.triggeredAt = LocalDateTime.now();
    }

    public void dismiss() {
        this.status = ReminderStatus.DISMISSED;
    }

    public void snooze(LocalDateTime newTime) {
        this.status = ReminderStatus.SNOOZED;
        this.scheduledTime = newTime;
    }

    public boolean isDue() {
        return status == ReminderStatus.PENDING && 
               LocalDateTime.now().isAfter(scheduledTime);
    }

    public boolean isOverdue() {
        return status == ReminderStatus.PENDING && 
               LocalDateTime.now().isAfter(scheduledTime.plusMinutes(5));
    }

    public long getMinutesUntilDue() {
        if (status != ReminderStatus.PENDING) return Long.MAX_VALUE;
        return java.time.Duration.between(LocalDateTime.now(), scheduledTime).toMinutes();
    }

    public boolean shouldReschedule() {
        return recurring && status == ReminderStatus.TRIGGERED;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", status=" + status +
                ", type=" + type +
                '}';
    }

    public enum ReminderStatus {
        PENDING,
        TRIGGERED,
        DISMISSED,
        SNOOZED,
        CANCELLED
    }

    public enum ReminderType {
        TASK_REMINDER,
        MEETING_REMINDER,
        BREAK_REMINDER,
        CUSTOM_REMINDER,
        SYSTEM_NOTIFICATION
    }
} 