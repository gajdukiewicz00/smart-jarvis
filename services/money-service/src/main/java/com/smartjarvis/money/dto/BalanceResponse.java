package com.smartjarvis.money.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for balance information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    /**
     * User ID
     */
    private String userId;

    /**
     * Total income for period
     */
    private BigDecimal totalIncome;

    /**
     * Total expenses for period
     */
    private BigDecimal totalExpenses;

    /**
     * Current balance (income - expenses)
     */
    private BigDecimal currentBalance;

    /**
     * Period description
     */
    private String period;

    /**
     * Currency code
     */
    private String currency;

    /**
     * Balance calculation timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Previous period balance for comparison
     */
    private BigDecimal previousBalance;

    /**
     * Balance change from previous period
     */
    private BigDecimal balanceChange;

    /**
     * Percentage change from previous period
     */
    private BigDecimal percentageChange;

    /**
     * Check if balance is positive
     */
    public boolean isPositive() {
        return currentBalance != null && currentBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Get formatted balance
     */
    public String getFormattedBalance() {
        if (currentBalance == null) return "0.00 RUB";
        String sign = currentBalance.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s%.2f %s", sign, currentBalance, currency != null ? currency : "RUB");
    }
}
