package com.gymcrm.workload.service;

import com.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.model.MonthWorkload;
import com.gymcrm.workload.model.TrainerWorkload;
import com.gymcrm.workload.model.YearWorkload;
import com.gymcrm.workload.storage.WorkloadStorage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculates and stores trainer monthly training summaries in MongoDB:
 * Trainer → Years → Months → trainingSummaryDuration.
 */
@Service
public class WorkloadService {

    private final WorkloadStorage storage;
    private final TrainerWorkloadEventService eventService;

    public WorkloadService(WorkloadStorage storage, TrainerWorkloadEventService eventService) {
        this.storage = storage;
        this.eventService = eventService;
    }

    /**
     * Delegates ADD/DELETE workload events to {@link TrainerWorkloadEventService}.
     */
    public void applyTrainingEvent(WorkloadUpdateRequest request) {
        eventService.processEvent(request);
    }

    /**
     * Returns the nested summary for a trainer (optionally filtered by year/month).
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
                .map(duration -> duration != null ? duration : 0)
                .orElse(0);
    }

    private static TrainerWorkloadResponse toResponse(TrainerWorkload trainer,
                                                      Integer yearFilter,
                                                      Integer monthFilter) {
        TrainerWorkloadResponse response = new TrainerWorkloadResponse();
        response.setTrainerUsername(trainer.getTrainerUsername());
        response.setTrainerFirstName(trainer.getTrainerFirstName());
        response.setTrainerLastName(trainer.getTrainerLastName());
        response.setTrainerStatus(trainer.getTrainerStatus());

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
