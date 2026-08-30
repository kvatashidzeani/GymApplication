package com.gymcrm.workload.service;

import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.storage.InMemoryWorkloadStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WorkloadServiceTest {

    private WorkloadService service;

    @BeforeEach
    void setUp() {
        InMemoryWorkloadStorage storage = new InMemoryWorkloadStorage();
        TrainerWorkloadEventService eventService = new TrainerWorkloadEventService(storage);
        service = new WorkloadService(storage, eventService);
    }

    @Test
    void applyTrainingEvent_add_buildsNestedYearMonthSummary() {
        service.applyTrainingEvent(request(ActionType.ADD, LocalDate.of(2026, 3, 15), 60));
        service.applyTrainingEvent(request(ActionType.ADD, LocalDate.of(2026, 3, 20), 30));

        TrainerWorkloadResponse response = service.getWorkload("Mike.Brown", 2026, 3);

        assertEquals("Mike.Brown", response.getTrainerUsername());
        assertEquals("Mike", response.getTrainerFirstName());
        assertEquals("Brown", response.getTrainerLastName());
        assertEquals(Boolean.TRUE, response.getTrainerStatus());
        assertEquals(1, response.getYears().size());
        assertEquals(2026, response.getYears().get(0).getYear());
        assertEquals(1, response.getYears().get(0).getMonths().size());
        assertEquals(3, response.getYears().get(0).getMonths().get(0).getMonth());
        assertEquals(90, response.getYears().get(0).getMonths().get(0).getTrainingSummaryDuration());
    }

    @Test
    void applyTrainingEvent_delete_decrementsDuration() {
        service.applyTrainingEvent(request(ActionType.ADD, LocalDate.of(2026, 3, 15), 90));
        service.applyTrainingEvent(request(ActionType.DELETE, LocalDate.of(2026, 3, 15), 30));

        assertEquals(60, service.getMonthDuration("Mike.Brown", 2026, 3));
    }

    @Test
    void applyTrainingEvent_deleteToZero_removesTrainer() {
        service.applyTrainingEvent(request(ActionType.ADD, LocalDate.of(2026, 3, 15), 30));
        service.applyTrainingEvent(request(ActionType.DELETE, LocalDate.of(2026, 3, 15), 30));

        assertThrows(TrainerWorkloadNotFoundException.class,
                () -> service.getWorkload("Mike.Brown", null, null));
    }

    @Test
    void getMonthDuration_missing_returnsZero() {
        assertEquals(0, service.getMonthDuration("Mike.Brown", 2026, 1));
    }

    @Test
    void getWorkload_multipleYears_nestedLists() {
        service.applyTrainingEvent(request(ActionType.ADD, LocalDate.of(2025, 12, 1), 45));
        service.applyTrainingEvent(request(ActionType.ADD, LocalDate.of(2026, 1, 10), 60));

        TrainerWorkloadResponse response = service.getWorkload("Mike.Brown", null, null);

        assertEquals(2, response.getYears().size());
        assertEquals(2025, response.getYears().get(0).getYear());
        assertEquals(2026, response.getYears().get(1).getYear());
    }

    private static WorkloadUpdateRequest request(ActionType action, LocalDate date, int duration) {
        WorkloadUpdateRequest req = new WorkloadUpdateRequest();
        req.setTrainerUsername("Mike.Brown");
        req.setTrainerFirstName("Mike");
        req.setTrainerLastName("Brown");
        req.setIsActive(true);
        req.setTrainingDate(date);
        req.setTrainingDuration(duration);
        req.setActionType(action);
        return req;
    }
}
