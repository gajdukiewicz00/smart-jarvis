package com.smartjarvis.money.controller;

import com.smartjarvis.money.dto.*;
import com.smartjarvis.money.service.MoneyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for financial operations
 */
@RestController
@RequestMapping("/api/v1/money")
@RequiredArgsConstructor
@Slf4j
public class MoneyController {

    private final MoneyService moneyService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "money-service",
            "version", "1.0.0-SNAPSHOT",
            "timestamp", Instant.now().toString()
        ));
    }

    // Transaction endpoints
    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> addTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        log.info("Adding transaction: type={}, amount={}, description='{}'", 
                request.getType(), request.getAmount(), request.getDescription());
        
        TransactionResponse response = moneyService.addTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Getting transactions: userId={}, from={}, to={}, page={}, size={}", 
                userId, from, to, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> transactions = moneyService.getTransactions(userId, from, to, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable String id,
            @RequestParam String userId) {
        
        log.debug("Getting transaction: id={}, userId={}", id, userId);
        
        TransactionResponse response = moneyService.getTransactionById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable String id,
            @Valid @RequestBody UpdateTransactionRequest request,
            @RequestParam String userId) {
        
        log.info("Updating transaction: id={}, userId={}", id, userId);
        
        TransactionResponse response = moneyService.updateTransaction(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable String id,
            @RequestParam String userId) {
        
        log.info("Deleting transaction: id={}, userId={}", id, userId);
        
        moneyService.deleteTransaction(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Balance and reports
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@RequestParam String userId) {
        log.debug("Getting balance for user: {}", userId);
        
        BalanceResponse balance = moneyService.getCurrentBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/reports/expenses")
    public ResponseEntity<ExpenseReportResponse> getExpenseReport(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        log.debug("Generating expense report: userId={}, from={}, to={}", userId, from, to);
        
        ExpenseReportResponse report = moneyService.getExpenseReport(userId, from, to);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/categories")
    public ResponseEntity<List<CategorySpendingResponse>> getCategorySpending(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        log.debug("Getting category spending: userId={}, from={}, to={}", userId, from, to);
        
        List<CategorySpendingResponse> spending = moneyService.getCategorySpending(userId, from, to);
        return ResponseEntity.ok(spending);
    }

    // Quick actions for voice commands
    @PostMapping("/expense")
    public ResponseEntity<TransactionResponse> addExpense(
            @RequestParam String userId,
            @RequestParam String description,
            @RequestParam String amount,
            @RequestParam(required = false) String category) {
        
        log.info("Adding expense via voice: userId={}, description='{}', amount={}", 
                userId, description, amount);
        
        try {
            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setUserId(userId);
            request.setDescription(description);
            request.setAmount(new java.math.BigDecimal(amount));
            request.setType(com.smartjarvis.money.domain.TransactionType.EXPENSE);
            request.setCategory(category != null ? category : "Прочее");
            
            TransactionResponse response = moneyService.addTransaction(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/income")
    public ResponseEntity<TransactionResponse> addIncome(
            @RequestParam String userId,
            @RequestParam String description,
            @RequestParam String amount,
            @RequestParam(required = false) String category) {
        
        log.info("Adding income via voice: userId={}, description='{}', amount={}", 
                userId, description, amount);
        
        try {
            CreateTransactionRequest request = new CreateTransactionRequest();
            request.setUserId(userId);
            request.setDescription(description);
            request.setAmount(new java.math.BigDecimal(amount));
            request.setType(com.smartjarvis.money.domain.TransactionType.INCOME);
            request.setCategory(category != null ? category : "Доход");
            
            TransactionResponse response = moneyService.addTransaction(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
