package com.gymcrm.workload.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerWorkloadModelTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validDocument_passesValidation() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Brown", "Mike", "Brown", true);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertTrue(violations.isEmpty());
    }

    @Test
    void missingTrainerStatus_failsValidation() {
        TrainerWorkload workload = new TrainerWorkload();
        workload.setTrainerUsername("Mike.Brown");
        workload.setTrainerFirstName("Mike");
        workload.setTrainerLastName("Brown");

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "trainerStatus".equals(v.getPropertyPath().toString())));
    }

    @Test
    void trainerStatus_isBooleanType() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Brown", "Mike", "Brown", Boolean.FALSE);

        assertEquals(Boolean.FALSE, workload.getTrainerStatus());
    }
}
