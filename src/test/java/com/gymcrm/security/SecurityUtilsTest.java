package com.gymcrm.security;

import com.gymcrm.exceptions.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void currentUsername_whenAuthenticated() {
        SecurityTestUtils.authenticate("Ani.Smith");
        assertEquals("Ani.Smith", SecurityUtils.currentUsername());
    }

    @Test
    void currentUsername_whenAnonymous_throws() {
        assertThrows(UnauthorizedException.class, SecurityUtils::currentUsername);
    }

    @Test
    void requireSelf_matching() {
        SecurityTestUtils.authenticate("Ani.Smith");
        assertEquals("Ani.Smith", SecurityUtils.requireSelf("Ani.Smith"));
    }

    @Test
    void requireSelf_mismatch_throws() {
        SecurityTestUtils.authenticate("Ani.Smith");
        assertThrows(UnauthorizedException.class, () -> SecurityUtils.requireSelf("Other.User"));
    }
}
