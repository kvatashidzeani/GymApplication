package com.gymcrm.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TraineeValidator {
    private static final Logger log = LoggerFactory.getLogger(TraineeValidator.class);


    public void validateTrainee(String firstName, String lastName,
                                LocalDate dateOfBirth, String address) {
        if (firstName == null || firstName.trim().isEmpty()) {
            log.error("First name cannot be null or empty");
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            log.error("Last name cannot be null or empty");
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        // dateOfBirth and address are optional for registration
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            log.error("Date of birth cannot be in the future");
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
    }
}
