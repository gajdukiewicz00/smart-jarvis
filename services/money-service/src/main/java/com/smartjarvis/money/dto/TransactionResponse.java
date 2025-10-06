package com.smartjarvis.money.dto;

import com.smartjarvis.money.domain.PaymentMethod;
import com.smartjarvis.money.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private String id;
    private String userId;
    private String description;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private PaymentMethod paymentMethod;
    private LocalDateTime transactionDate;
    private String currency;
    private String location;
    private String merchant;
    private String[] tags;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Get signed amount (negative for expenses)
     */
    public BigDecimal getSignedAmount() {
        return type == TransactionType.EXPENSE ? amount.negate() : amount;
    }

    /**
     * Get formatted amount with currency
     */
    public String getFormattedAmount() {
        String sign = type == TransactionType.EXPENSE ? "-" : "+";
        return String.format("%s%.2f %s", sign, amount, currency != null ? currency : "RUB");
    }
}
