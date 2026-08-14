package com.gymcrm.logging;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Transaction-level correlation id for a single REST request / business flow.
 * Stored in SLF4J MDC so all logs for the same call share the same id,
 * and exposed via {@value #HEADER} for downstream services.
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
