package com.smartjarvis.todo.domain;

/**
 * Todo status enumeration
 */
public enum TodoStatus {
    PENDING("В ожидании"),
    IN_PROGRESS("В процессе"),
    COMPLETED("Выполнено"),
    CANCELLED("Отменено");

    private final String displayName;

    TodoStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
