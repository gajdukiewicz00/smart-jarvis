package com.smartjarvis.calendar.domain;

/**
 * Calendar event priority enumeration
 */
public enum EventPriority {
    LOW(1, "Низкий"),
    NORMAL(2, "Обычный"),
    HIGH(3, "Высокий"),
    URGENT(4, "Срочный");

    private final int level;
    private final String displayName;

    EventPriority(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }
}
