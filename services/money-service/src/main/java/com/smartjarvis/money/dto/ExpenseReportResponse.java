package com.smartjarvis.money.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for expense reports
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseReportResponse {

    private String userId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalExpenses;
    private int transactionCount;
    private List<CategorySpendingResponse> categoryBreakdown;
    private BigDecimal averagePerDay;
    private String currency;
}
