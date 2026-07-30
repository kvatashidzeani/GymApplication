package com.gymcrm.validators;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidationTest {

    @Test
    void requireNonBlank_trims() {
        assertEquals("Ani", RequestValidation.requireNonBlank("  Ani  ", "First name"));
    }

    @Test
    void requireNonBlank_blank_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> RequestValidation.requireNonBlank("   ", "Password"));
    }

    @Test
    void requireSameUsername_mismatch_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> RequestValidation.requireSameUsername("A.B", "C.D"));
    }

    @Test
    void requirePeriodOrder_invalid_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> RequestValidation.requirePeriodOrder(
                        LocalDate.of(2024, 12, 1), LocalDate.of(2024, 11, 1)));
    }
}
