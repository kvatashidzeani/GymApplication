package com.gymcrm.Util;

import com.gymcrm.dao.UserDao;
import com.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CredentialGeneratorsTest {

    @Nested
    class IdGeneratorTests {

        @Test
        void generateNextId_incrementsFromOne() {
            IdGenerator generator = new IdGenerator();
            assertEquals(1L, generator.generateNextId());
            assertEquals(2L, generator.generateNextId());
        }

        @Test
        void initialize_setsCounterAboveMaxExistingId() {
            IdGenerator generator = new IdGenerator();
            Map<Long, Object> existing = new HashMap<>();
            existing.put(5L, new Object());
            existing.put(10L, new Object());
            generator.initialize(existing);
            assertEquals(11L, generator.generateNextId());
        }
    }

    @Nested
    class PasswordGeneratorTests {

        @Test
        void generatePassword_hasExpectedLength() {
            PasswordGenerator generator = new PasswordGenerator();
            String password = generator.generatePassword();
            assertEquals(10, password.length());
        }

        @Test
        void generatePassword_isNotBlank() {
            PasswordGenerator generator = new PasswordGenerator();
            assertFalse(generator.generatePassword().isBlank());
        }
    }

    @Nested
    class UsernameGeneratorTests {

        private UserDao userDao;
        private UsernameGenerator usernameGenerator;

        @BeforeEach
        void setUp() {
            userDao = mock(UserDao.class);
            usernameGenerator = new UsernameGenerator();
            usernameGenerator.setUserDao(userDao);
        }

        @Test
        void generateUsername_usesFirstDotLast() {
            when(userDao.findAll()).thenReturn(List.of());
            assertEquals("Ani.Smith", usernameGenerator.generateUsername("Ani", "Smith"));
        }

        @Test
        void generateUsername_appendsCounterWhenTaken() {
            User existing = new User("Ani", "Smith", "Ani.Smith", "x", true, 1L);
            when(userDao.findAll()).thenReturn(List.of(existing));
            assertEquals("Ani.Smith1", usernameGenerator.generateUsername("Ani", "Smith"));
        }

        @Test
        void generateUsername_trimsNames() {
            when(userDao.findAll()).thenReturn(List.of());
            assertEquals("Ani.Smith", usernameGenerator.generateUsername("  Ani ", " Smith "));
        }
    }
}
