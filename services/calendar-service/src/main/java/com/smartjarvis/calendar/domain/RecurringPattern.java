package com.smartjarvis.calendar.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Recurring pattern for calendar events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPattern {

    /**
     * Recurrence frequency
     */
    private RecurringFrequency frequency;

    /**
     * Interval for recurrence (e.g., every 2 weeks)
     */
    @Builder.Default
    private int interval = 1;

    /**
     * Days of week for weekly recurrence
     */
    private List<DayOfWeek> daysOfWeek;

    /**
     * Day of month for monthly recurrence (1-31)
     */
    private Integer dayOfMonth;

    /**
     * Week of month for monthly recurrence (1-4, -1 for last)
     */
    private Integer weekOfMonth;

    /**
     * Month of year for yearly recurrence (1-12)
     */
    private Integer monthOfYear;

    /**
     * End date for recurrence
     */
    private LocalDateTime endDate;

    /**
     * Maximum number of occurrences
     */
    private Integer maxOccurrences;

    /**
     * Number of completed occurrences
     */
    @Builder.Default
    private int completedOccurrences = 0;

    /**
     * Whether pattern is active
     */
    @Builder.Default
    private boolean isActive = true;

    /**
     * Exceptions (dates to skip)
     */
    private List<LocalDateTime> exceptions;

    public enum RecurringFrequency {
        DAILY("Ежедневно"),
        WEEKLY("Еженедельно"),
        MONTHLY("Ежемесячно"),
        YEARLY("Ежегодно");

        private final String displayName;

        RecurringFrequency(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Calculate next occurrence date
     */
    public LocalDateTime calculateNextOccurrence(LocalDateTime baseDate) {
        if (!isActive) return null;
        
        switch (frequency) {
            case DAILY -> {
                return baseDate.plusDays(interval);
            }
            case WEEKLY -> {
                return baseDate.plusWeeks(interval);
            }
            case MONTHLY -> {
                if (dayOfMonth != null) {
                    return baseDate.plusMonths(interval).withDayOfMonth(
                        Math.min(dayOfMonth, baseDate.plusMonths(interval).toLocalDate().lengthOfMonth())
                    );
                }
                return baseDate.plusMonths(interval);
            }
            case YEARLY -> {
                return baseDate.plusYears(interval);
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Check if pattern has reached its end
     */
    public boolean hasReachedEnd() {
        if (endDate != null && LocalDateTime.now().isAfter(endDate)) {
            return true;
        }
        
        if (maxOccurrences != null && completedOccurrences >= maxOccurrences) {
            return true;
        }
        
        return false;
    }
}
