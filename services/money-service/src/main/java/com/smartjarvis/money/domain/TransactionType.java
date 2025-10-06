package com.smartjarvis.money.domain;

/**
 * Transaction type enumeration
 */
public enum TransactionType {
    INCOME("Доход"),
    EXPENSE("Расход");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
