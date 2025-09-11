package com.smartjarvis.money.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for category spending information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySpendingResponse {

    private String category;
    private BigDecimal amount;
    private int transactionCount;
    private BigDecimal percentage;
    private String currency;
}
