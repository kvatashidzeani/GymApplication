package com.gymcrm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserCredentialGeneratorTest {

    private UserCredentialGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new UserCredentialGenerator();
    }

    @Test
    void generateUsername_withoutDuplicates_returnsBaseFormat() {
        String username = generator.generateUsername("Ani", "Smith", 0);
        assertEquals("Ani.Smith", username);
    }

    @Test
    void generateUsername_withDuplicates_appendsSerialSuffix() {
        String username = generator.generateUsername("Ani", "Smith", 2);
        assertEquals("Ani.Smith2", username);
    }

    @Test
    void generatePassword_returnsTenCharacters() {
        String password = generator.generatePassword();
        assertEquals(10, password.length());
    }

    @Test
    void generatePassword_containsOnlyAlphanumericCharacters() {
        String password = generator.generatePassword();
        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }
}
