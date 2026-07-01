package com.gymcrm.validators;

import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TrainerValidator{
    private static final Logger log = LoggerFactory.getLogger(TrainerValidator.class);


    public void validateTrainer(String firstName, String lastName, TrainingType specialization) {
        if (firstName == null || firstName.trim().isEmpty()) {
            log.error("First name validation failed: {}", firstName);
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            log.error("Last name validation failed: {}", lastName);
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (specialization == null) {
            log.error("Specialization is null");
            throw new IllegalArgumentException("Specialization cannot be null");
        }

        log.debug("Input validation passed for: {} {}", firstName, lastName);
    }
}