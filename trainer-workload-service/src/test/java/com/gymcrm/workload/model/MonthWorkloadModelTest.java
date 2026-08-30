package com.gymcrm.workload.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MonthWorkloadModelTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validMonth_passesValidation() {
        MonthWorkload monthWorkload = new MonthWorkload(8, 60);

        Set<ConstraintViolation<MonthWorkload>> violations = validator.validate(monthWorkload);

        assertTrue(violations.isEmpty());
        assertEquals(Integer.valueOf(60), monthWorkload.getTrainingSummaryDuration());
    }

    @Test
    void negativeDuration_failsValidation() {
        MonthWorkload monthWorkload = new MonthWorkload(8, -1);

        Set<ConstraintViolation<MonthWorkload>> violations = validator.validate(monthWorkload);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> "trainingSummaryDuration".equals(v.getPropertyPath().toString())));
    }

    @Test
    void trainingSummaryDuration_isIntegerNumberType() {
        MonthWorkload monthWorkload = new MonthWorkload();
        monthWorkload.setMonth(3);
        monthWorkload.setTrainingSummaryDuration(90);

        assertEquals(Integer.class, monthWorkload.getTrainingSummaryDuration().getClass());
    }
}
