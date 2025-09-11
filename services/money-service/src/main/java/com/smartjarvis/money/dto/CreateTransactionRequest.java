package com.smartjarvis.money.dto;

import com.smartjarvis.money.domain.PaymentMethod;
import com.smartjarvis.money.domain.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating new transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    /**
     * User ID who owns the transaction
     */
    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    /**
     * Transaction description
     */
    @NotBlank(message = "Description cannot be blank")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Transaction amount (always positive)
     */
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    /**
     * Transaction type
     */
    @NotNull(message = "Transaction type cannot be null")
    private TransactionType type;

    /**
     * Transaction category
     */
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    /**
     * Payment method
     */
    private PaymentMethod paymentMethod;

    /**
     * Transaction date and time
     */
    private LocalDateTime transactionDate;

    /**
     * Currency code (ISO 4217)
     */
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    /**
     * Location where transaction occurred
     */
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    /**
     * Merchant/store name
     */
    @Size(max = 200, message = "Merchant must not exceed 200 characters")
    private String merchant;

    /**
     * Tags for categorization
     */
    private String[] tags;

    /**
     * Additional notes
     */
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
