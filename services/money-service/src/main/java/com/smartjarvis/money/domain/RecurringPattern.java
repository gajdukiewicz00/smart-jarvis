package com.smartjarvis.money.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Recurring pattern for transactions
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
     * Day of month for monthly recurrence
     */
    private Integer dayOfMonth;

    /**
     * Day of week for weekly recurrence
     */
    private Integer dayOfWeek;

    /**
     * End date for recurrence
     */
    private LocalDateTime endDate;

    /**
     * Maximum number of occurrences
     */
    private Integer maxOccurrences;

    /**
     * Next scheduled date
     */
    private LocalDateTime nextDate;

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
}
