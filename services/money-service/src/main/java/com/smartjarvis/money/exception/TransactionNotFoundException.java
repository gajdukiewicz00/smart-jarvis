package com.smartjarvis.money.exception;

/**
 * Exception thrown when transaction is not found
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found with id: " + transactionId);
    }

    public TransactionNotFoundException(String transactionId, String userId) {
        super("Transaction not found with id: " + transactionId + " for user: " + userId);
    }
}
