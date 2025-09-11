package com.smartjarvis.money.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction category entity
 */
@Document(collection = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    private String id;

    /**
     * User who owns this category
     */
    private String userId;

    /**
     * Category name
     */
    private String name;

    /**
     * Category description
     */
    private String description;

    /**
     * Category type
     */
    private CategoryType type;

    /**
     * Category color (hex code)
     */
    @Builder.Default
    private String color = "#3B82F6";

    /**
     * Category icon/emoji
     */
    private String icon;

    /**
     * Parent category ID for hierarchy
     */
    private String parentId;

    /**
     * Budget limit for this category
     */
    private BigDecimal budgetLimit;

    /**
     * Budget period
     */
    @Builder.Default
    private BudgetPeriod budgetPeriod = BudgetPeriod.MONTHLY;

    /**
     * Whether category is active
     */
    @Builder.Default
    private boolean isActive = true;

    /**
     * Whether category is system-defined
     */
    @Builder.Default
    private boolean isSystem = false;

    /**
     * Sort order
     */
    @Builder.Default
    private int sortOrder = 0;

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

    public enum CategoryType {
        INCOME("Доход"),
        EXPENSE("Расход"),
        BOTH("Универсальная");

        private final String displayName;

        CategoryType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BudgetPeriod {
        DAILY("Ежедневно"),
        WEEKLY("Еженедельно"),
        MONTHLY("Ежемесячно"),
        YEARLY("Ежегодно");

        private final String displayName;

        BudgetPeriod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Check if category has budget limit
     */
    public boolean hasBudgetLimit() {
        return budgetLimit != null && budgetLimit.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if category is root (no parent)
     */
    public boolean isRoot() {
        return parentId == null || parentId.trim().isEmpty();
    }
}
