package com.gymcrm.workload.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class JmsTransactionLoggingTest {

    private final JmsTransactionLogging jmsTransactionLogging = new JmsTransactionLogging();

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void execute_setsTransactionIdFromHeader() {
        AtomicReference<String> txDuringCallback = new AtomicReference<>();

        jmsTransactionLogging.execute("jms-tx-1", "JMS test", () -> {
            txDuringCallback.set(TransactionContext.get());
            return "processed";
        });

        assertEquals("jms-tx-1", txDuringCallback.get());
        assertNull(TransactionContext.get());
    }

    @Test
    void execute_generatesTransactionIdWhenMissing() {
        AtomicReference<String> txDuringCallback = new AtomicReference<>();

        jmsTransactionLogging.execute(null, "JMS test", () -> {
            txDuringCallback.set(TransactionContext.get());
            return "processed";
        });

        assertNotNull(txDuringCallback.get());
        assertFalse(txDuringCallback.get().isBlank());
        assertNull(TransactionContext.get());
    }

    @Test
    void execute_marksDlqOutcomeWithoutThrowing() {
        jmsTransactionLogging.execute(null, "JMS test",
                () -> JmsTransactionLogging.dlqDetail("invalid payload"));
        assertNull(TransactionContext.get());
    }

    @Test
    void execute_clearsContextOnError() {
        assertThrows(IllegalStateException.class, () ->
                jmsTransactionLogging.execute("jms-tx-err", "JMS test", () -> {
                    throw new IllegalStateException("boom");
                }));
        assertNull(TransactionContext.get());
    }
}
