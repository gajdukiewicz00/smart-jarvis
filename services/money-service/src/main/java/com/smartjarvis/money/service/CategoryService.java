package com.smartjarvis.money.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Category management service
 */
@Service
@Slf4j
public class CategoryService {

    // Default categories for MVP
    private final Set<String> defaultCategories = Set.of(
        "Еда", "Транспорт", "Развлечения", "Покупки", "Коммунальные", 
        "Здоровье", "Образование", "Подарки", "Путешествия", "Прочее"
    );

    /**
     * Validate that category exists (simplified for MVP)
     */
    public void validateCategoryExists(String userId, String category) {
        if (category != null && !defaultCategories.contains(category)) {
            log.warn("Unknown category used: {} for user: {}", category, userId);
            // For MVP, allow any category
        }
    }

    /**
     * Get default categories
     */
    public Set<String> getDefaultCategories() {
        return defaultCategories;
    }
}
