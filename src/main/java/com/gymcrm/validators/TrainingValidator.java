package com.gymcrm.validators;


import com.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TrainingValidator {
    private final Logger log = LoggerFactory.getLogger(TrainingValidator.class);

    public void validateTraining(Long traineeId, Long trainerId, String trainingName,
                                 TrainingType trainingType, LocalDate trainingDate,
                                 Integer duration) {
        if (trainerId == null) {
            log.error("Trainer ID is null");
            throw new IllegalArgumentException("Trainer ID cannot be null");
        }
        if (trainingName == null || trainingName.trim().isEmpty()) {
            log.error("Training name validation failed: {}", trainingName);
            throw new IllegalArgumentException("Training name cannot be null or empty");
        }
        if (trainingType == null) {
            log.error("Training type is null");
            throw new IllegalArgumentException("Training type cannot be null");
        }
        if (trainingDate == null) {
            log.error("Training date is null");
            throw new IllegalArgumentException("Training date cannot be null");
        }
        if (duration == null || duration <= 0) {
            log.error("Duration validation failed: {}", duration);
            throw new IllegalArgumentException("Duration must be positive");
        }
        log.debug("Input validation passed for training: {}", trainingName);
    }
}