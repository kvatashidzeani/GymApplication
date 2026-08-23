package com.gymcrm.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies BCrypt hashing: unique salt per encode, and matches() against stored hash.
 */
class PasswordHashingTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void encode_producesBcryptHashDifferentFromPlaintext() {
        String hash = encoder.encode("mySecret12");

        assertNotEquals("mySecret12", hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    @Test
    void encode_samePassword_yieldsDifferentHashes_dueToSalt() {
        String hash1 = encoder.encode("samePassword");
        String hash2 = encoder.encode("samePassword");

        assertNotEquals(hash1, hash2);
        assertTrue(encoder.matches("samePassword", hash1));
        assertTrue(encoder.matches("samePassword", hash2));
    }

    @Test
    void matches_rejectsWrongPassword() {
        String hash = encoder.encode("correct");

        assertFalse(encoder.matches("wrong", hash));
    }

    @Test
    void passwordConfig_bean_isBcrypt() {
        PasswordEncoder bean = new PasswordConfig().passwordEncoder();
        assertInstanceOf(BCryptPasswordEncoder.class, bean);

        String hash = bean.encode("test");
        assertTrue(bean.matches("test", hash));
    }
}
