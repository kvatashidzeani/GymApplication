package com.gymcrm.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionLoggingTest {

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

class SensitiveDataMaskerTest {

    @Test
    void maskQuery_hidesPasswords() {
        String masked = SensitiveDataMasker.maskQuery("username=Ani.Smith&password=secret&x=1");
        assertEquals("username=Ani.Smith&password=***&x=1", masked);
    }

    @Test
    void maskBody_hidesPasswordFields() {
        String masked = SensitiveDataMasker.maskBody(
                "{\"username\":\"Ani\",\"oldPassword\":\"a\",\"newPassword\":\"b\"}");
        assertTrue(masked.contains("\"oldPassword\":\"***\""));
        assertTrue(masked.contains("\"newPassword\":\"***\""));
        assertTrue(masked.contains("\"Ani\""));
    }
}
