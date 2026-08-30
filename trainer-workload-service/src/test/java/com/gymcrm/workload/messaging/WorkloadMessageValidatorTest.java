package com.gymcrm.workload.messaging;

import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkloadMessageValidatorTest {

    private WorkloadMessageValidator validator;

    @BeforeEach
    void setUp() {
        jakarta.validation.Validator jakartaValidator =
                jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        validator = new WorkloadMessageValidator(jakartaValidator);
    }

    @Test
    void validate_completeRequest_returnsNoErrors() {
        assertTrue(validator.validate(sampleRequest()).isEmpty());
    }

    @Test
    void validate_nullRequest_returnsError() {
        List<String> errors = validator.validate(null);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("required"));
    }

    @Test
    void validate_missingTrainerUsername_returnsError() {
        WorkloadUpdateRequest request = sampleRequest();
        request.setTrainerUsername(null);

        List<String> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("trainerUsername"));
    }

    @Test
    void validate_missingTrainerFirstName_returnsError() {
        WorkloadUpdateRequest request = sampleRequest();
        request.setTrainerFirstName("");

        List<String> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("trainerFirstName"));
    }

    @Test
    void validate_missingTrainerLastName_returnsError() {
        WorkloadUpdateRequest request = sampleRequest();
        request.setTrainerLastName(null);

        List<String> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("trainerLastName"));
    }

    @Test
    void validate_missingIsActive_returnsError() {
        WorkloadUpdateRequest request = sampleRequest();
        request.setIsActive(null);

        List<String> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("isActive"));
    }

    @Test
    void validate_missingTrainingDate_returnsError() {
        WorkloadUpdateRequest request = sampleRequest();
        request.setTrainingDate(null);

        List<String> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("trainingDate"));
    }

    @Test
    void validate_missingActionType_returnsError() {
        WorkloadUpdateRequest request = sampleRequest();
        request.setActionType(null);

        List<String> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("actionType"));
    }

    @Test
    void validate_missingDuration_returnsError() {
        WorkloadUpdateRequest request = sampleRequest();
        request.setTrainingDuration(null);

        List<String> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("trainingDuration"));
    }

    @Test
    void validate_nonPositiveDuration_returnsError() {
        WorkloadUpdateRequest request = sampleRequest();
        request.setTrainingDuration(0);

        List<String> errors = validator.validate(request);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("trainingDuration"));
    }

    private static WorkloadUpdateRequest sampleRequest() {
        WorkloadUpdateRequest request = new WorkloadUpdateRequest();
        request.setTrainerUsername("Mike.Brown");
        request.setTrainerFirstName("Mike");
        request.setTrainerLastName("Brown");
        request.setIsActive(true);
        request.setTrainingDate(LocalDate.of(2026, 8, 29));
        request.setTrainingDuration(60);
        request.setActionType(ActionType.ADD);
        return request;
    }
}
