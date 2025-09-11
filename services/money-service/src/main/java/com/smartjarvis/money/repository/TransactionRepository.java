package com.smartjarvis.money.repository;

import com.smartjarvis.money.domain.Transaction;
import com.smartjarvis.money.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Transaction entities
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    /**
     * Find transactions by user ID with pagination
     */
    Page<Transaction> findByUserIdOrderByTransactionDateDesc(String userId, Pageable pageable);

    /**
     * Find transactions by user and date range
     */
    @Query("{'userId': ?0, 'transactionDate': {'$gte': ?1, '$lte': ?2}}")
    List<Transaction> findByUserIdAndDateRange(String userId, LocalDateTime from, LocalDateTime to);

    /**
     * Find transactions by user, type and date range
     */
    @Query("{'userId': ?0, 'type': ?1, 'transactionDate': {'$gte': ?2, '$lte': ?3}}")
    List<Transaction> findByUserIdAndTypeAndDateRange(String userId, TransactionType type, 
                                                     LocalDateTime from, LocalDateTime to);

    /**
     * Find transactions by category
     */
    List<Transaction> findByUserIdAndCategoryOrderByTransactionDateDesc(String userId, String category);

    /**
     * Find transactions by user and security check
     */
    Optional<Transaction> findByIdAndUserId(String id, String userId);

    /**
     * Delete transaction with user security check
     */
    void deleteByIdAndUserId(String id, String userId);

    /**
     * Check if transaction exists for user
     */
    boolean existsByIdAndUserId(String id, String userId);

    /**
     * Calculate total amount by type and date range
     */
    @Aggregation(pipeline = {
        "{ '$match': { 'userId': ?0, 'type': ?1, 'transactionDate': { '$gte': ?2, '$lte': ?3 } } }",
        "{ '$group': { '_id': null, 'total': { '$sum': '$amount' } } }"
    })
    Optional<BigDecimal> calculateTotalByTypeAndDateRange(String userId, TransactionType type, 
                                                         LocalDateTime from, LocalDateTime to);

    /**
     * Calculate total by category and date range
     */
    @Aggregation(pipeline = {
        "{ '$match': { 'userId': ?0, 'category': ?1, 'transactionDate': { '$gte': ?2, '$lte': ?3 } } }",
        "{ '$group': { '_id': null, 'total': { '$sum': '$amount' } } }"
    })
    Optional<BigDecimal> calculateTotalByCategoryAndDateRange(String userId, String category,
                                                             LocalDateTime from, LocalDateTime to);

    /**
     * Get category spending summary
     */
    @Aggregation(pipeline = {
        "{ '$match': { 'userId': ?0, 'type': 'EXPENSE', 'transactionDate': { '$gte': ?1, '$lte': ?2 } } }",
        "{ '$group': { '_id': '$category', 'total': { '$sum': '$amount' }, 'count': { '$sum': 1 } } }",
        "{ '$sort': { 'total': -1 } }"
    })
    List<CategorySpendingSummary> getCategorySpendingSummary(String userId, LocalDateTime from, LocalDateTime to);

    /**
     * Get monthly spending trend
     */
    @Aggregation(pipeline = {
        "{ '$match': { 'userId': ?0, 'type': 'EXPENSE', 'transactionDate': { '$gte': ?1 } } }",
        "{ '$group': { " +
        "    '_id': { " +
        "      'year': { '$year': '$transactionDate' }, " +
        "      'month': { '$month': '$transactionDate' } " +
        "    }, " +
        "    'total': { '$sum': '$amount' }, " +
        "    'count': { '$sum': 1 } " +
        "} }",
        "{ '$sort': { '_id.year': 1, '_id.month': 1 } }"
    })
    List<MonthlySpendingSummary> getMonthlySpendingTrend(String userId, LocalDateTime from);

    /**
     * Find recurring transactions
     */
    @Query("{'userId': ?0, 'isRecurring': true, 'recurringPattern.isActive': true}")
    List<Transaction> findActiveRecurringTransactions(String userId);

    /**
     * Find transactions by payment method
     */
    List<Transaction> findByUserIdAndPaymentMethodOrderByTransactionDateDesc(String userId, PaymentMethod paymentMethod);

    /**
     * Count transactions by type for user
     */
    long countByUserIdAndType(String userId, TransactionType type);

    /**
     * Find recent transactions (last N days)
     */
    @Query("{'userId': ?0, 'transactionDate': {'$gte': ?1}}")
    List<Transaction> findRecentTransactions(String userId, LocalDateTime since);

    /**
     * Category spending summary projection
     */
    interface CategorySpendingSummary {
        String getId(); // category name
        BigDecimal getTotal();
        Long getCount();
    }

    /**
     * Monthly spending summary projection
     */
    interface MonthlySpendingSummary {
        MonthYear getId();
        BigDecimal getTotal();
        Long getCount();
        
        interface MonthYear {
            int getYear();
            int getMonth();
        }
    }
}
