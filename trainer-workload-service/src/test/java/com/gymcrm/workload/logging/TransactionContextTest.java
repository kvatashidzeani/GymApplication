package com.gymcrm.workload.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionContextTest {

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void getOrCreate_usesIncomingHeader() {
        assertEquals("abc-123", TransactionContext.getOrCreate("abc-123"));
    }

    @Test
    void getOrCreate_generatesWhenMissing() {
        String id = TransactionContext.getOrCreate(null);
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    void set_putsValueInMdc() {
        TransactionContext.set("tx-1");
        assertEquals("tx-1", TransactionContext.get());
        TransactionContext.clear();
        assertNull(TransactionContext.get());
    }
}
