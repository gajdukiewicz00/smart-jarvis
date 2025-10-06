package com.smartjarvis.calendar.domain;

/**
 * Calendar event status enumeration
 */
public enum EventStatus {
    TENTATIVE("Предварительно"),
    CONFIRMED("Подтверждено"),
    CANCELLED("Отменено");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
