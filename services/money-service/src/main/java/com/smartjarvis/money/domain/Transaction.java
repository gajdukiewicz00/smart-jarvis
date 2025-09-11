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
import java.util.Currency;

/**
 * Financial transaction entity
 */
@Document(collection = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    private String id;

    /**
     * User who owns this transaction
     */
    private String userId;

    /**
     * Transaction description
     */
    private String description;

    /**
     * Transaction amount (always positive, type determines income/expense)
     */
    private BigDecimal amount;

    /**
     * Transaction type
     */
    private TransactionType type;

    /**
     * Transaction category
     */
    private String category;

    /**
     * Payment method used
     */
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    /**
     * Transaction date and time
     */
    private LocalDateTime transactionDate;

    /**
     * Currency code (ISO 4217)
     */
    @Builder.Default
    private String currency = "RUB";

    /**
     * Exchange rate if different from base currency
     */
    private BigDecimal exchangeRate;

    /**
     * Location where transaction occurred
     */
    private String location;

    /**
     * Merchant/store name
     */
    private String merchant;

    /**
     * Tags for additional categorization
     */
    private String[] tags;

    /**
     * Receipt or document reference
     */
    private String receipt;

    /**
     * Notes or additional information
     */
    private String notes;

    /**
     * Whether transaction is recurring
     */
    @Builder.Default
    private boolean isRecurring = false;

    /**
     * Recurring pattern if applicable
     */
    private RecurringPattern recurringPattern;

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

    /**
     * Get amount with sign based on type
     */
    public BigDecimal getSignedAmount() {
        return type == TransactionType.EXPENSE ? amount.negate() : amount;
    }

    /**
     * Check if transaction is expense
     */
    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    /**
     * Check if transaction is income
     */
    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    /**
     * Check if transaction is from today
     */
    public boolean isFromToday() {
        return transactionDate != null && 
               transactionDate.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    /**
     * Check if transaction is from current month
     */
    public boolean isFromCurrentMonth() {
        if (transactionDate == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return transactionDate.getYear() == now.getYear() &&
               transactionDate.getMonthValue() == now.getMonthValue();
    }
}
