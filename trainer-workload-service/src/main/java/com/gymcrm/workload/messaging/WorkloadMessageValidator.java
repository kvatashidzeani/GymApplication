package com.gymcrm.workload.messaging;

import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validates required workload event fields before applying business logic.
 */
@Component
public class WorkloadMessageValidator {

    private final Validator validator;

    public WorkloadMessageValidator(Validator validator) {
        this.validator = validator;
    }

    public List<String> validate(WorkloadUpdateRequest request) {
        if (request == null) {
            return List.of("request: body is required");
        }

        Set<ConstraintViolation<WorkloadUpdateRequest>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return List.of();
        }

        List<String> errors = new ArrayList<>(violations.size());
        for (ConstraintViolation<WorkloadUpdateRequest> violation : violations) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        return errors;
    }
}
