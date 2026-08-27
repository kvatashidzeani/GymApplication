package com.gymcrm.workload.logging;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Correlation id shared with Gym CRM via {@code X-Transaction-Id}.
 */
public final class TransactionContext {

    public static final String MDC_KEY = "transactionId";
    public static final String HEADER = "X-Transaction-Id";

    private TransactionContext() {
    }

    public static String getOrCreate(String incoming) {
        if (incoming != null && !incoming.isBlank()) {
            return incoming.trim();
        }
        return UUID.randomUUID().toString();
    }

    public static void set(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            MDC.remove(MDC_KEY);
        } else {
            MDC.put(MDC_KEY, transactionId);
        }
    }

    public static String get() {
        return MDC.get(MDC_KEY);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
