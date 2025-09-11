package com.smartjarvis.money.service;

import com.smartjarvis.money.domain.Transaction;
import com.smartjarvis.money.domain.TransactionType;
import com.smartjarvis.money.dto.*;
import com.smartjarvis.money.exception.TransactionNotFoundException;
import com.smartjarvis.money.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Money service with comprehensive financial operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MoneyService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Add new transaction
     */
    public TransactionResponse addTransaction(CreateTransactionRequest request) {
        log.info("Adding transaction: type={}, amount={}, userId={}", 
                request.getType(), request.getAmount(), request.getUserId());

        // Validate category exists
        if (request.getCategory() != null) {
            categoryService.validateCategoryExists(request.getUserId(), request.getCategory());
        }

        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
                .description(request.getDescription())
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .paymentMethod(request.getPaymentMethod())
                .transactionDate(request.getTransactionDate() != null ? 
                               request.getTransactionDate() : LocalDateTime.now())
                .currency(request.getCurrency() != null ? request.getCurrency() : "RUB")
                .location(request.getLocation())
                .merchant(request.getMerchant())
                .tags(request.getTags())
                .notes(request.getNotes())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Publish transaction event
        publishTransactionCreatedEvent(savedTransaction);

        // Check budget limits
        checkBudgetLimits(savedTransaction);

        log.info("Transaction added successfully: id={}", savedTransaction.getId());
        return mapToResponse(savedTransaction);
    }

    /**
     * Get transactions with filtering and pagination
     */
    public Page<TransactionResponse> getTransactions(String userId, LocalDate from, LocalDate to, Pageable pageable) {
        log.debug("Getting transactions for user: {}, from: {}, to: {}", userId, from, to);

        Page<Transaction> transactions;
        
        if (from != null && to != null) {
            LocalDateTime fromDateTime = from.atStartOfDay();
            LocalDateTime toDateTime = to.atTime(23, 59, 59);
            
            List<Transaction> filteredTransactions = transactionRepository
                    .findByUserIdAndDateRange(userId, fromDateTime, toDateTime);
            
            // Convert to page (simplified for MVP)
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
        } else {
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
        }

        return transactions.map(this::mapToResponse);
    }

    /**
     * Get transaction by ID
     */
    public TransactionResponse getTransactionById(String id, String userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return mapToResponse(transaction);
    }

    /**
     * Update transaction
     */
    public TransactionResponse updateTransaction(String id, UpdateTransactionRequest request, String userId) {
        log.info("Updating transaction: id={}, userId={}", id, userId);

        Transaction transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        // Update fields
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            categoryService.validateCategoryExists(userId, request.getCategory());
            transaction.setCategory(request.getCategory());
        }
        if (request.getPaymentMethod() != null) {
            transaction.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
        if (request.getLocation() != null) {
            transaction.setLocation(request.getLocation());
        }
        if (request.getMerchant() != null) {
            transaction.setMerchant(request.getMerchant());
        }
        if (request.getTags() != null) {
            transaction.setTags(request.getTags());
        }
        if (request.getNotes() != null) {
            transaction.setNotes(request.getNotes());
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);

        // Publish update event
        publishTransactionUpdatedEvent(updatedTransaction);

        log.info("Transaction updated successfully: id={}", updatedTransaction.getId());
        return mapToResponse(updatedTransaction);
    }

    /**
     * Delete transaction
     */
    public void deleteTransaction(String id, String userId) {
        log.info("Deleting transaction: id={}, userId={}", id, userId);

        if (!transactionRepository.existsByIdAndUserId(id, userId)) {
            throw new TransactionNotFoundException(id);
        }

        transactionRepository.deleteByIdAndUserId(id, userId);

        // Publish delete event
        publishTransactionDeletedEvent(id, userId);

        log.info("Transaction deleted successfully: id={}", id);
    }

    /**
     * Get current balance
     */
    public BalanceResponse getCurrentBalance(String userId) {
        log.debug("Calculating balance for user: {}", userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);

        // Calculate totals
        BigDecimal totalIncome = transactionRepository
                .calculateTotalByTypeAndDateRange(userId, TransactionType.INCOME, startOfMonth, now)
                .orElse(BigDecimal.ZERO);

        BigDecimal totalExpenses = transactionRepository
                .calculateTotalByTypeAndDateRange(userId, TransactionType.EXPENSE, startOfMonth, now)
                .orElse(BigDecimal.ZERO);

        BigDecimal currentBalance = totalIncome.subtract(totalExpenses);

        return BalanceResponse.builder()
                .userId(userId)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .currentBalance(currentBalance)
                .period("current_month")
                .currency("RUB")
                .timestamp(now)
                .build();
    }

    /**
     * Get expense report
     */
    public ExpenseReportResponse getExpenseReport(String userId, LocalDate from, LocalDate to) {
        log.debug("Generating expense report: user={}, from={}, to={}", userId, from, to);

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        List<Transaction> expenses = transactionRepository
                .findByUserIdAndTypeAndDateRange(userId, TransactionType.EXPENSE, fromDateTime, toDateTime);

        BigDecimal totalExpenses = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Category breakdown
        var categorySpending = transactionRepository
                .getCategorySpendingSummary(userId, fromDateTime, toDateTime);

        return ExpenseReportResponse.builder()
                .userId(userId)
                .fromDate(from)
                .toDate(to)
                .totalExpenses(totalExpenses)
                .transactionCount(expenses.size())
                .categoryBreakdown(categorySpending.stream()
                        .map(cs -> CategorySpendingResponse.builder()
                                .category(cs.getId())
                                .amount(cs.getTotal())
                                .transactionCount(cs.getCount().intValue())
                                .build())
                        .collect(Collectors.toList()))
                .averagePerDay(totalExpenses.divide(
                        BigDecimal.valueOf(from.until(to).getDays() + 1), 
                        2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * Get category spending
     */
    public List<CategorySpendingResponse> getCategorySpending(String userId, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(23, 59, 59);

        var categorySpending = transactionRepository
                .getCategorySpendingSummary(userId, fromDateTime, toDateTime);

        return categorySpending.stream()
                .map(cs -> CategorySpendingResponse.builder()
                        .category(cs.getId())
                        .amount(cs.getTotal())
                        .transactionCount(cs.getCount().intValue())
                        .percentage(calculateCategoryPercentage(cs.getTotal(), userId, fromDateTime, toDateTime))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Calculate category percentage of total spending
     */
    private BigDecimal calculateCategoryPercentage(BigDecimal categoryAmount, String userId, 
                                                  LocalDateTime from, LocalDateTime to) {
        BigDecimal totalExpenses = transactionRepository
                .calculateTotalByTypeAndDateRange(userId, TransactionType.EXPENSE, from, to)
                .orElse(BigDecimal.ZERO);

        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return categoryAmount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Map Transaction to response DTO
     */
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .paymentMethod(transaction.getPaymentMethod())
                .transactionDate(transaction.getTransactionDate())
                .currency(transaction.getCurrency())
                .location(transaction.getLocation())
                .merchant(transaction.getMerchant())
                .tags(transaction.getTags())
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }

    /**
     * Check budget limits and send alerts
     */
    private void checkBudgetLimits(Transaction transaction) {
        // TODO: Implement budget checking logic
        log.debug("Checking budget limits for transaction: {}", transaction.getId());
    }

    /**
     * Publish transaction created event
     */
    private void publishTransactionCreatedEvent(Transaction transaction) {
        try {
            kafkaTemplate.send("money.transaction.created", transaction.getId(), transaction);
            log.debug("Transaction created event published: id={}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to publish transaction created event: id={}", transaction.getId(), e);
        }
    }

    /**
     * Publish transaction updated event
     */
    private void publishTransactionUpdatedEvent(Transaction transaction) {
        try {
            kafkaTemplate.send("money.transaction.updated", transaction.getId(), transaction);
            log.debug("Transaction updated event published: id={}", transaction.getId());
        } catch (Exception e) {
            log.error("Failed to publish transaction updated event: id={}", transaction.getId(), e);
        }
    }

    /**
     * Publish transaction deleted event
     */
    private void publishTransactionDeletedEvent(String transactionId, String userId) {
        try {
            kafkaTemplate.send("money.transaction.deleted", transactionId, 
                    Map.of("transactionId", transactionId, "userId", userId));
            log.debug("Transaction deleted event published: id={}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish transaction deleted event: id={}", transactionId, e);
        }
    }
}
