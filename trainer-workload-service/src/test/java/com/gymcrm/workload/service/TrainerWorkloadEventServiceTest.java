package com.gymcrm.workload.service;

import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.storage.InMemoryWorkloadStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainerWorkloadEventServiceTest {

    private TrainerWorkloadEventService eventService;
    private InMemoryWorkloadStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryWorkloadStorage();
        eventService = new TrainerWorkloadEventService(storage);
    }

    @Test
    void processAddEvent_newTrainer_createsYearMonthWithTrainingDuration() {
        eventService.processAddEvent(request(ActionType.ADD, LocalDate.of(2026, 8, 29), 60));

        assertEquals(60, storage.findByUsername("Mike.Brown")
                .flatMap(t -> t.findYear(2026))
                .flatMap(y -> y.findMonth(8))
                .map(m -> m.getTrainingSummaryDuration())
                .orElse(-1));
    }

    @Test
    void processAddEvent_existingTrainer_addsDurationToMatchingMonth() {
        eventService.processAddEvent(request(ActionType.ADD, LocalDate.of(2026, 8, 29), 60));
        eventService.processAddEvent(request(ActionType.ADD, LocalDate.of(2026, 8, 30), 45));

        assertEquals(105, storage.findByUsername("Mike.Brown")
                .flatMap(t -> t.findYear(2026))
                .flatMap(y -> y.findMonth(8))
                .map(m -> m.getTrainingSummaryDuration())
                .orElse(-1));
    }

    @Test
    void processAddEvent_existingTrainer_newMonth_createsMonthAndAddsDuration() {
        eventService.processAddEvent(request(ActionType.ADD, LocalDate.of(2026, 8, 1), 60));
        eventService.processAddEvent(request(ActionType.ADD, LocalDate.of(2026, 9, 1), 30));

        assertEquals(60, storage.findByUsername("Mike.Brown")
                .flatMap(t -> t.findYear(2026))
                .flatMap(y -> y.findMonth(8))
                .map(m -> m.getTrainingSummaryDuration())
                .orElse(-1));
        assertEquals(30, storage.findByUsername("Mike.Brown")
                .flatMap(t -> t.findYear(2026))
                .flatMap(y -> y.findMonth(9))
                .map(m -> m.getTrainingSummaryDuration())
                .orElse(-1));
    }

    @Test
    void processDeleteEvent_subtractsDuration() {
        eventService.processAddEvent(request(ActionType.ADD, LocalDate.of(2026, 3, 15), 90));
        eventService.processEvent(request(ActionType.DELETE, LocalDate.of(2026, 3, 15), 30));

        assertEquals(60, storage.findByUsername("Mike.Brown")
                .flatMap(t -> t.findYear(2026))
                .flatMap(y -> y.findMonth(3))
                .map(m -> m.getTrainingSummaryDuration())
                .orElse(-1));
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
