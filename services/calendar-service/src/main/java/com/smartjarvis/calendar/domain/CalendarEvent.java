package com.smartjarvis.calendar.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Calendar event entity
 */
@Document(collection = "calendar_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent {

    @Id
    private String id;

    /**
     * User who owns this event
     */
    private String userId;

    /**
     * Event title
     */
    private String title;

    /**
     * Event description
     */
    private String description;

    /**
     * Event start date and time
     */
    private LocalDateTime startTime;

    /**
     * Event end date and time
     */
    private LocalDateTime endTime;

    /**
     * Event location
     */
    private String location;

    /**
     * Event status
     */
    @Builder.Default
    private EventStatus status = EventStatus.CONFIRMED;

    /**
     * Event priority
     */
    @Builder.Default
    private EventPriority priority = EventPriority.NORMAL;

    /**
     * Event category/type
     */
    private String category;

    /**
     * Event attendees (email addresses or names)
     */
    private List<String> attendees;

    /**
     * Reminders for this event
     */
    private List<Reminder> reminders;

    /**
     * Recurring pattern if applicable
     */
    private RecurringPattern recurringPattern;

    /**
     * Whether event is all-day
     */
    @Builder.Default
    private boolean isAllDay = false;

    /**
     * Whether event is private
     */
    @Builder.Default
    private boolean isPrivate = false;

    /**
     * Event color (hex code)
     */
    @Builder.Default
    private String color = "#3B82F6";

    /**
     * Tags for categorization
     */
    private String[] tags;

    /**
     * External event ID (for sync with other calendars)
     */
    private String externalId;

    /**
     * Calendar ID (for multiple calendars)
     */
    @Builder.Default
    private String calendarId = "default";

    /**
     * Creation timestamp
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Get event duration in minutes
     */
    public long getDurationMinutes() {
        if (startTime == null || endTime == null) return 0;
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Check if event is happening now
     */
    public boolean isHappeningNow() {
        LocalDateTime now = LocalDateTime.now();
        return startTime != null && endTime != null &&
               (now.isEqual(startTime) || now.isAfter(startTime)) &&
               (now.isEqual(endTime) || now.isBefore(endTime));
    }

    /**
     * Check if event is today
     */
    public boolean isToday() {
        return startTime != null && 
               startTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    /**
     * Check if event is upcoming (within next 24 hours)
     */
    public boolean isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return startTime != null && 
               startTime.isAfter(now) && 
               startTime.isBefore(now.plusDays(1));
    }

    /**
     * Check if event conflicts with another event
     */
    public boolean conflictsWith(CalendarEvent other) {
        if (other == null || startTime == null || endTime == null ||
            other.startTime == null || other.endTime == null) {
            return false;
        }

        return (startTime.isBefore(other.endTime) || startTime.isEqual(other.endTime)) &&
               (endTime.isAfter(other.startTime) || endTime.isEqual(other.startTime));
    }
}
