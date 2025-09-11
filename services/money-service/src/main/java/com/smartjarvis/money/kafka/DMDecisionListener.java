package com.smartjarvis.money.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartjarvis.money.domain.PaymentMethod;
import com.smartjarvis.money.domain.TransactionType;
import com.smartjarvis.money.dto.CreateTransactionRequest;
import com.smartjarvis.money.service.MoneyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Kafka listener for DM decision events
 * Processes money-related decisions from Dialog Manager
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DMDecisionListener {

    private final MoneyService moneyService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "dm.decision", groupId = "money-service")
    public void handleDecision(String message) {
        try {
            Map<?,?> decisionEvent = objectMapper.readValue(message, Map.class);
            // Only process decisions for money-service
            if (!"money-service".equals(decisionEvent.get("targetService"))) {
                return;
            }

            String sessionId = (String) decisionEvent.get("sessionId");
            String userId = (String) decisionEvent.get("userId");
            String action = (String) decisionEvent.get("action");
            @SuppressWarnings("unchecked")
            Map<String,String> parameters = (Map<String,String>) decisionEvent.get("parameters");

            log.info("Processing money decision: sessionId={}, action={}", sessionId, action);

            switch (action) {
                case "add_expense" -> handleAddExpense(userId, parameters);
                case "add_income" -> handleAddIncome(userId, parameters);
                case "get_balance" -> handleGetBalance(userId);
                case "get_expense_report" -> handleGetExpenseReport(userId, parameters);
                case "get_income_report" -> handleGetIncomeReport(userId, parameters);
                case "get_financial_stats" -> handleGetFinancialStats(userId);
                case "get_category_spending" -> handleGetCategorySpending(userId, parameters);
                default -> log.warn("Unknown money action: {}", decisionEvent.getAction());
            }

        } catch (Exception e) {
            log.error("Failed to process money decision message", e);
        }
    }

    private void handleAddExpense(String userId, Map<String,String> parameters) {
        String amountStr = parameters.get("amount");
        String description = parameters.get("description");
        String category = parameters.get("category");
        String paymentMethodStr = parameters.get("payment_method");

        if (amountStr == null || description == null) {
            log.warn("Missing required parameters for expense: amount={}, description={}", amountStr, description);
            return;
        }

        try {
            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setUserId(userId);
            request.setDescription(description);
            request.setAmount(new BigDecimal(amountStr));
            request.setType(TransactionType.EXPENSE);
            request.setCategory(category != null ? category : "Прочее");
            
            if (paymentMethodStr != null) {
                request.setPaymentMethod(PaymentMethod.valueOf(paymentMethodStr));
            }

            moneyService.addTransaction(request);
            log.info("Expense added via voice: amount={}, description='{}'", amountStr, description);

        } catch (Exception e) {
            log.error("Failed to add expense via voice: amount={}, description='{}'", amountStr, description, e);
        }
    }

    private void handleAddIncome(String userId, Map<String,String> parameters) {
        String amountStr = parameters.get("amount");
        String description = parameters.get("description");

        if (amountStr == null || description == null) {
            log.warn("Missing required parameters for income: amount={}, description={}", amountStr, description);
            return;
        }

        try {
            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setUserId(userId);
            request.setDescription(description);
            request.setAmount(new BigDecimal(amountStr));
            request.setType(TransactionType.INCOME);
            request.setCategory("Доход");

            moneyService.addTransaction(request);
            log.info("Income added via voice: amount={}, description='{}'", amountStr, description);

        } catch (Exception e) {
            log.error("Failed to add income via voice: amount={}, description='{}'", amountStr, description, e);
        }
    }

    private void handleGetBalance(String userId) {
        try {
            var balance = moneyService.getCurrentBalance(userId);
            log.info("Balance retrieved via voice: userId={}, balance={}", 
                    userId, balance.getFormattedBalance());

            // TODO: Send balance info to TTS for voice response
            
        } catch (Exception e) {
            log.error("Failed to get balance via voice: userId={}", userId, e);
        }
    }

    private void handleGetExpenseReport(String userId, Map<String,String> parameters) {
        String period = parameters.get("period");
        
        try {
            // Calculate date range based on period
            LocalDate to = LocalDate.now();
            LocalDate from = switch (period != null ? period : "month") {
                case "today" -> to;
                case "week" -> to.minusWeeks(1);
                case "month" -> to.minusMonths(1);
                case "year" -> to.minusYears(1);
                default -> to.minusMonths(1);
            };

            var report = moneyService.getExpenseReport(userId, from, to);
            log.info("Expense report generated via voice: userId={}, period={}, total={}", 
                    userId, period, report.getTotalExpenses());

            // TODO: Send report summary to TTS for voice response
            
        } catch (Exception e) {
            log.error("Failed to generate expense report via voice: userId={}", userId, e);
        }
    }

    private void handleGetIncomeReport(String userId, Map<String,String> parameters) {
        // Similar to expense report but for income
        log.info("Income report requested via voice: userId={}", userId);
        // TODO: Implement income report logic
    }

    private void handleGetFinancialStats(String userId) {
        // Financial statistics overview
        log.info("Financial stats requested via voice: userId={}", userId);
        // TODO: Implement comprehensive financial statistics
    }

    private void handleGetCategorySpending(String userId, Map<String,String> parameters) {
        String category = parameters.get("category");
        
        try {
            LocalDate to = LocalDate.now();
            LocalDate from = to.minusMonths(1); // Last month
            
            var categorySpending = moneyService.getCategorySpending(userId, from, to);
            log.info("Category spending retrieved via voice: userId={}, categories={}", 
                    userId, categorySpending.size());

            // TODO: Send category breakdown to TTS for voice response
            
        } catch (Exception e) {
            log.error("Failed to get category spending via voice: userId={}", userId, e);
        }
    }
}
