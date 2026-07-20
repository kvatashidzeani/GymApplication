package com.gymcrm.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TraineeValidatorTest {

    private TraineeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TraineeValidator();
    }

    @Test
    void validateTrainee_success() {
        assertDoesNotThrow(() -> validator.validateTrainee(
                "Ani", "Smith", LocalDate.of(2000, 1, 1), "Tbilisi"));
    }

    @Test
    void validateTrainee_nullFirstName_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validateTrainee(null, "Smith", LocalDate.of(2000, 1, 1), "Tbilisi"));
    }

    @Test
    void validateTrainee_futureDateOfBirth_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validateTrainee("Ani", "Smith", LocalDate.now().plusDays(1), "Tbilisi"));
    }

    @Test
    void validateTrainee_emptyAddress_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validateTrainee("Ani", "Smith", LocalDate.of(2000, 1, 1), "  "));
    }
}
