package com.gymcrm.workload.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Transaction-level logging for JMS consumers (ActiveMQ).
 * Reuses {@code X-Transaction-Id} from Gym CRM when present; otherwise generates one.
 */
@Component
public class JmsTransactionLogging {

    private static final Logger TX_LOG = LoggerFactory.getLogger("com.gymcrm.workload.logging.Transaction");
    public static final String DLQ_PREFIX = "DLQ: ";

    public enum Outcome {
        OK,
        DLQ,
        ERROR
    }

    public void execute(String incomingTransactionId, String operation, JmsTransactionCallback callback) {
        String transactionId = TransactionContext.getOrCreate(incomingTransactionId);
        TransactionContext.set(transactionId);
        long started = System.currentTimeMillis();
        TX_LOG.info("Transaction started transactionId={} operation={}", transactionId, operation);
        Outcome outcome = Outcome.OK;
        String detail = "processed";
        try {
            detail = callback.run();
            if (detail != null && detail.startsWith(DLQ_PREFIX)) {
                outcome = Outcome.DLQ;
            }
        } catch (RuntimeException ex) {
            outcome = Outcome.ERROR;
            detail = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            throw ex;
        } finally {
            long durationMs = System.currentTimeMillis() - started;
            if (outcome == Outcome.ERROR) {
                TX_LOG.warn(
                        "Transaction finished transactionId={} operation={} status={} detail={} durationMs={}",
                        transactionId, operation, outcome, detail, durationMs);
            } else {
                TX_LOG.info(
                        "Transaction finished transactionId={} operation={} status={} detail={} durationMs={}",
                        transactionId, operation, outcome, detail, durationMs);
            }
            TransactionContext.clear();
        }
    }

    public static String dlqDetail(String reason) {
        return DLQ_PREFIX + reason;
    }

    @FunctionalInterface
    public interface JmsTransactionCallback {
        String run();
    }
}
