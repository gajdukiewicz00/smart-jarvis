package com.smartjarvis.money.dto;

import com.smartjarvis.money.domain.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for updating transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionRequest {

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
    private BigDecimal amount;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private PaymentMethod paymentMethod;
    private LocalDateTime transactionDate;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 200, message = "Merchant must not exceed 200 characters")
    private String merchant;

    private String[] tags;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
