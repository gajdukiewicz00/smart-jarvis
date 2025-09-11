package com.smartjarvis.money.kafka;

import com.smartjarvis.events.DMDecisionEvent;
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

    @KafkaListener(topics = "dm.decision", groupId = "money-service")
    public void handleDecision(DMDecisionEvent decisionEvent) {
        try {
            // Only process decisions for money-service
            if (!"money-service".equals(decisionEvent.getTargetService())) {
                return;
            }

            log.info("Processing money decision: sessionId={}, action={}", 
                    decisionEvent.getSessionId(), decisionEvent.getAction());

            switch (decisionEvent.getAction()) {
                case "add_expense" -> handleAddExpense(decisionEvent);
                case "add_income" -> handleAddIncome(decisionEvent);
                case "get_balance" -> handleGetBalance(decisionEvent);
                case "get_expense_report" -> handleGetExpenseReport(decisionEvent);
                case "get_income_report" -> handleGetIncomeReport(decisionEvent);
                case "get_financial_stats" -> handleGetFinancialStats(decisionEvent);
                case "get_category_spending" -> handleGetCategorySpending(decisionEvent);
                default -> log.warn("Unknown money action: {}", decisionEvent.getAction());
            }

        } catch (Exception e) {
            log.error("Failed to process money decision: sessionId={}", 
                    decisionEvent.getSessionId(), e);
        }
    }

    private void handleAddExpense(DMDecisionEvent decision) {
        String amountStr = decision.getParameters().get("amount");
        String description = decision.getParameters().get("description");
        String category = decision.getParameters().get("category");
        String paymentMethodStr = decision.getParameters().get("payment_method");

        if (amountStr == null || description == null) {
            log.warn("Missing required parameters for expense: amount={}, description={}", amountStr, description);
            return;
        }

        try {
            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setUserId(decision.getUserId());
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

    private void handleAddIncome(DMDecisionEvent decision) {
        String amountStr = decision.getParameters().get("amount");
        String description = decision.getParameters().get("description");

        if (amountStr == null || description == null) {
            log.warn("Missing required parameters for income: amount={}, description={}", amountStr, description);
            return;
        }

        try {
            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setUserId(decision.getUserId());
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

    private void handleGetBalance(DMDecisionEvent decision) {
        try {
            var balance = moneyService.getCurrentBalance(decision.getUserId());
            log.info("Balance retrieved via voice: userId={}, balance={}", 
                    decision.getUserId(), balance.getFormattedBalance());

            // TODO: Send balance info to TTS for voice response
            
        } catch (Exception e) {
            log.error("Failed to get balance via voice: userId={}", decision.getUserId(), e);
        }
    }

    private void handleGetExpenseReport(DMDecisionEvent decision) {
        String period = decision.getParameters().get("period");
        
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

            var report = moneyService.getExpenseReport(decision.getUserId(), from, to);
            log.info("Expense report generated via voice: userId={}, period={}, total={}", 
                    decision.getUserId(), period, report.getTotalExpenses());

            // TODO: Send report summary to TTS for voice response
            
        } catch (Exception e) {
            log.error("Failed to generate expense report via voice: userId={}", decision.getUserId(), e);
        }
    }

    private void handleGetIncomeReport(DMDecisionEvent decision) {
        // Similar to expense report but for income
        log.info("Income report requested via voice: userId={}", decision.getUserId());
        // TODO: Implement income report logic
    }

    private void handleGetFinancialStats(DMDecisionEvent decision) {
        // Financial statistics overview
        log.info("Financial stats requested via voice: userId={}", decision.getUserId());
        // TODO: Implement comprehensive financial statistics
    }

    private void handleGetCategorySpending(DMDecisionEvent decision) {
        String category = decision.getParameters().get("category");
        
        try {
            LocalDate to = LocalDate.now();
            LocalDate from = to.minusMonths(1); // Last month
            
            var categorySpending = moneyService.getCategorySpending(decision.getUserId(), from, to);
            log.info("Category spending retrieved via voice: userId={}, categories={}", 
                    decision.getUserId(), categorySpending.size());

            // TODO: Send category breakdown to TTS for voice response
            
        } catch (Exception e) {
            log.error("Failed to get category spending via voice: userId={}", decision.getUserId(), e);
        }
    }
}
