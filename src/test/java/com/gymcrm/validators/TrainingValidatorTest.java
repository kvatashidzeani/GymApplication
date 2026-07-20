package com.gymcrm.validators;

import com.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingValidatorTest {

    private TrainingValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TrainingValidator();
    }

    @Test
    void validateTraining_success() {
        assertDoesNotThrow(() -> validator.validateTraining(
                1L, 2L, "Morning Cardio", new TrainingType("Cardio", 1L),
                LocalDate.of(2026, 1, 1), 60));
    }

    @Test
    void validateTraining_nullTrainerId_throws() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateTraining(
                1L, null, "Session", new TrainingType("Cardio", 1L), LocalDate.now(), 60));
    }

    @Test
    void validateTraining_zeroDuration_throws() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateTraining(
                1L, 2L, "Session", new TrainingType("Cardio", 1L), LocalDate.now(), 0));
    }

    @Test
    void validateTraining_nullTrainingDate_throws() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateTraining(
                1L, 2L, "Session", new TrainingType("Cardio", 1L), null, 60));
    }
}
