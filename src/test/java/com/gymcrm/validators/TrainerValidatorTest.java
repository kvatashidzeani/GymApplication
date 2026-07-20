package com.gymcrm.validators;

import com.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrainerValidatorTest {

    private TrainerValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TrainerValidator();
    }

    @Test
    void validateTrainer_success() {
        assertDoesNotThrow(() -> validator.validateTrainer(
                "Giorgi", "Janelidze", new TrainingType("Cardio", 1L)));
    }

    @Test
    void validateTrainer_nullSpecialization_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validateTrainer("Giorgi", "Janelidze", null));
    }

    @Test
    void validateTrainer_emptyLastName_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validateTrainer("Giorgi", "", new TrainingType("Cardio", 1L)));
    }
}
