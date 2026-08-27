package com.gymcrm.workload.service;

import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.model.MonthWorkload;
import com.gymcrm.workload.model.TrainerWorkload;
import com.gymcrm.workload.model.YearWorkload;
import com.gymcrm.workload.storage.InMemoryWorkloadStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculates and stores trainer monthly training summaries in the nested in-memory structure:
 * Trainer → Years → Months → trainingSummaryDuration.
 */
@Service
public class WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadService.class);

    private final InMemoryWorkloadStorage storage;

    public WorkloadService(InMemoryWorkloadStorage storage) {
        this.storage = storage;
    }

    /**
     * Applies training planned (ADD) or cancelled (DELETE) to the monthly summary.
     */
    public void applyTrainingEvent(WorkloadUpdateRequest request) {
        String username = request.getTrainerUsername().trim();
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();
        int duration = request.getTrainingDuration();

        TrainerWorkload trainer = storage.findByUsername(username)
                .orElseGet(() -> new TrainerWorkload(
                        username,
                        request.getTrainerFirstName().trim(),
                        request.getTrainerLastName().trim(),
                        Boolean.TRUE.equals(request.getIsActive())));

        trainer.setTrainerFirstName(request.getTrainerFirstName().trim());
        trainer.setTrainerLastName(request.getTrainerLastName().trim());
        trainer.setTrainerStatus(Boolean.TRUE.equals(request.getIsActive()));

        YearWorkload yearWorkload = trainer.getOrCreateYear(year);
        MonthWorkload monthWorkload = yearWorkload.getOrCreateMonth(month);

        if (request.getActionType() == ActionType.ADD) {
            monthWorkload.setTrainingSummaryDuration(
                    monthWorkload.getTrainingSummaryDuration() + duration);
            log.info("ADD {} min → trainer={} year={} month={} total={}",
                    duration, username, year, month, monthWorkload.getTrainingSummaryDuration());
        } else {
            int updated = Math.max(0, monthWorkload.getTrainingSummaryDuration() - duration);
            monthWorkload.setTrainingSummaryDuration(updated);
            log.info("DELETE {} min → trainer={} year={} month={} total={}",
                    duration, username, year, month, updated);
            yearWorkload.removeMonthIfEmpty(month);
            trainer.removeYearIfEmpty(year);
        }

        if (trainer.getYears().isEmpty() && request.getActionType() == ActionType.DELETE) {
            storage.delete(username);
            log.info("Removed empty trainer workload username={}", username);
        } else {
            storage.save(trainer);
        }
    }

    /**
     * Returns the nested in-memory summary for a trainer (optionally filtered by year/month).
     */
    public TrainerWorkloadResponse getWorkload(String trainerUsername, Integer year, Integer month) {
        TrainerWorkload trainer = storage.findByUsername(trainerUsername.trim())
                .orElseThrow(() -> new TrainerWorkloadNotFoundException(
                        "No workload found for trainer: " + trainerUsername));

        return toResponse(trainer, year, month);
    }

    public int getMonthDuration(String trainerUsername, int year, int month) {
        return storage.findByUsername(trainerUsername.trim())
                .flatMap(t -> t.findYear(year))
                .flatMap(y -> y.findMonth(month))
                .map(MonthWorkload::getTrainingSummaryDuration)
                .orElse(0);
    }

    private static TrainerWorkloadResponse toResponse(TrainerWorkload trainer,
                                                      Integer yearFilter,
                                                      Integer monthFilter) {
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setTrainerUsername(trainer.getTrainerUsername());
        response.setTrainerFirstName(trainer.getTrainerFirstName());
        response.setTrainerLastName(trainer.getTrainerLastName());
        response.setTrainerStatus(trainer.isTrainerStatus());

        List<YearWorkload> years = trainer.getYears();
        if (yearFilter != null) {
            years = years.stream()
                    .filter(y -> y.getYear() == yearFilter)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        List<TrainerWorkloadResponse.YearWorkloadDto> yearDtos = new ArrayList<>();
        for (YearWorkload yearWorkload : years) {
            TrainerWorkloadResponse.YearWorkloadDto yearDto =
                    new TrainerWorkloadResponse.YearWorkloadDto();
            yearDto.setYear(yearWorkload.getYear());

            List<MonthWorkload> months = yearWorkload.getMonths();
            if (monthFilter != null) {
                months = months.stream()
                        .filter(m -> m.getMonth() == monthFilter)
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            List<TrainerWorkloadResponse.MonthWorkloadDto> monthDtos = new ArrayList<>();
            for (MonthWorkload monthWorkload : months) {
                TrainerWorkloadResponse.MonthWorkloadDto monthDto =
                        new TrainerWorkloadResponse.MonthWorkloadDto();
                monthDto.setMonth(monthWorkload.getMonth());
                monthDto.setTrainingSummaryDuration(monthWorkload.getTrainingSummaryDuration());
                monthDtos.add(monthDto);
            }
            yearDto.setMonths(monthDtos);
            if (!monthDtos.isEmpty() || monthFilter == null) {
                yearDtos.add(yearDto);
            }
        }

        if (yearDtos.isEmpty() || yearDtos.stream().allMatch(y -> y.getMonths().isEmpty())) {
            throw new TrainerWorkloadNotFoundException(
                    "No workload found for trainer: " + trainer.getTrainerUsername());
        }

        response.setYears(yearDtos);
        return response;
    }
}
