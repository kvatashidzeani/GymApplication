package com.gymcrm.dto;

import com.gymcrm.logging.TransactionContext;

import java.time.Instant;

/**
 * Standard error body returned by {@code RestExceptionHandler} for all endpoints.
 */
public class ErrorResponse {

    private final String error;
    private final int status;
    private final String transactionId;
    private final String timestamp;

    public ErrorResponse(String error, int status) {
        this.error = error != null && !error.isBlank() ? error : "Unexpected error";
        this.status = status;
        this.transactionId = TransactionContext.get();
        this.timestamp = Instant.now().toString();
    }

    public String getError() {
        return error;
    }

    public int getStatus() {
        return status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
