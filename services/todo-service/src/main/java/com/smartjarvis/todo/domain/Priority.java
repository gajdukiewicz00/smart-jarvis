package com.smartjarvis.todo.domain;

/**
 * Todo priority enumeration
 */
public enum Priority {
    LOW(1, "Низкий"),
    NORMAL(2, "Обычный"),
    HIGH(3, "Высокий"),
    URGENT(4, "Срочный");

    private final int level;
    private final String displayName;

    Priority(int level, String displayName) {
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
