package com.gymcrm.workload.service;

import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.model.MonthWorkload;
import com.gymcrm.workload.model.TrainerWorkload;
import com.gymcrm.workload.model.YearWorkload;
import com.gymcrm.workload.storage.WorkloadStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Processes incoming workload events (ActiveMQ) and persists trainer summaries in MongoDB.
 * <p>
 * ADD flow (per assignment):
 * <ol>
 *   <li>Extract trainer document by username</li>
 *   <li>If missing — create document with year/month from training date and duration = training duration</li>
 *   <li>If present — locate year/month matching training date</li>
 *   <li>Add training duration to existing monthly summary</li>
 *   <li>Save document in MongoDB</li>
 * </ol>
 */
@Service
public class TrainerWorkloadEventService {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadEventService.class);

    private final WorkloadStorage storage;

    public TrainerWorkloadEventService(WorkloadStorage storage) {
        this.storage = storage;
    }

    public void processEvent(WorkloadUpdateRequest request) {
        if (request.getActionType() == ActionType.DELETE) {
            processDeleteEvent(request);
            return;
        }
        processAddEvent(request);
    }

    /**
     * Steps (a)–(e): handle training ADD event.
     */
    void processAddEvent(WorkloadUpdateRequest request) {
        String username = request.getTrainerUsername().trim();
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        int trainingDuration = request.getTrainingDuration();

        // (a) Extract trainer record by username
        Optional<TrainerWorkload> existing = storage.findByUsername(username);

        if (existing.isEmpty()) {
            // (b) New trainer — create year/month from training date, duration = training duration
            TrainerWorkload created = createTrainerWithFirstTraining(request, year, month, trainingDuration);
            storage.save(created);
            log.info("Created trainer summary username={} year={} month={} duration={}",
                    username, year, month, trainingDuration);
            return;
        }

        TrainerWorkload trainer = existing.get();
        syncTrainerProfile(trainer, request);

        // (c) Find correct year/month for the training date
        YearWorkload yearWorkload = trainer.getOrCreateYear(year);
        MonthWorkload monthWorkload = yearWorkload.getOrCreateMonth(month);

        // (d) Add training duration to existing monthly summary
        int currentDuration = monthWorkload.getTrainingSummaryDuration() != null
                ? monthWorkload.getTrainingSummaryDuration() : 0;
        int updatedDuration = currentDuration + trainingDuration;
        monthWorkload.setTrainingSummaryDuration(updatedDuration);

        // (e) Save updated document in MongoDB
        storage.save(trainer);
        log.info("Updated trainer summary username={} year={} month={} duration={} total={}",
                username, year, month, trainingDuration, updatedDuration);
    }

    private void processDeleteEvent(WorkloadUpdateRequest request) {
        String username = request.getTrainerUsername().trim();
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        int trainingDuration = request.getTrainingDuration();

        Optional<TrainerWorkload> existing = storage.findByUsername(username);
        if (existing.isEmpty()) {
            log.warn("DELETE ignored — no trainer summary for username={}", username);
            return;
        }

        TrainerWorkload trainer = existing.get();
        syncTrainerProfile(trainer, request);

        trainer.findYear(year).ifPresent(yearWorkload -> {
            yearWorkload.findMonth(month).ifPresent(monthWorkload -> {
                int currentDuration = monthWorkload.getTrainingSummaryDuration() != null
                        ? monthWorkload.getTrainingSummaryDuration() : 0;
                int updated = Math.max(0, currentDuration - trainingDuration);
                monthWorkload.setTrainingSummaryDuration(updated);
                log.info("DELETE {} min → trainer={} year={} month={} total={}",
                        trainingDuration, username, year, month, updated);
            });
            yearWorkload.removeMonthIfEmpty(month);
            trainer.removeYearIfEmpty(year);
        });

        if (trainer.getYears().isEmpty()) {
            storage.delete(username);
            log.info("Removed empty trainer summary username={}", username);
        } else {
            storage.save(trainer);
        }
    }

    private static TrainerWorkload createTrainerWithFirstTraining(WorkloadUpdateRequest request,
                                                                  int year,
                                                                  int month,
                                                                  int trainingDuration) {
        MonthWorkload monthWorkload = new MonthWorkload(month, trainingDuration);
        YearWorkload yearWorkload = new YearWorkload(year);
        yearWorkload.getMonths().add(monthWorkload);

        TrainerWorkload trainer = new TrainerWorkload(
                request.getTrainerUsername().trim(),
                request.getTrainerFirstName().trim(),
                request.getTrainerLastName().trim(),
                Boolean.TRUE.equals(request.getIsActive()));
        trainer.getYears().add(yearWorkload);
        return trainer;
    }

    private static void syncTrainerProfile(TrainerWorkload trainer, WorkloadUpdateRequest request) {
        trainer.setTrainerFirstName(request.getTrainerFirstName().trim());
        trainer.setTrainerLastName(request.getTrainerLastName().trim());
        trainer.setTrainerStatus(Boolean.TRUE.equals(request.getIsActive()));
    }
}
