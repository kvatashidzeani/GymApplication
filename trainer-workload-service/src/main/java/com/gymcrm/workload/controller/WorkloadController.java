package com.gymcrm.workload.controller;

import com.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.service.WorkloadService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST API for trainer monthly workload.
 */
@RestController
@RequestMapping("/workload")
public class WorkloadController {

    private static final Logger log = LoggerFactory.getLogger(WorkloadController.class);

    private final WorkloadService workloadService;

    public WorkloadController(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    /**
     * Browser-friendly hint. Workload updates must be sent as POST with JSON body.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> workloadUsage() {
        return ResponseEntity.ok(Map.of(
                "message", "Use POST /workload with JSON body to send trainer workload (ADD/DELETE).",
                "example", Map.of(
                        "trainerUsername", "Mike.Brown",
                        "trainerFirstName", "Mike",
                        "trainerLastName", "Brown",
                        "isActive", true,
                        "trainingDate", "2026-03-15",
                        "trainingDuration", 60,
                        "actionType", "ADD"
                )
        ));
    }

    /**
     * Accepts trainer workload when a training is planned or cancelled.
     * <p>
     * Contract — Request: trainerUsername, trainerFirstName, trainerLastName,
     * isActive, trainingDate, trainingDuration, actionType (ADD/DELETE).
     * Response: {@code 200 OK}.
     */
    @PostMapping
    public ResponseEntity<Void> updateWorkload(@Valid @RequestBody WorkloadUpdateRequest request) {
        log.info("POST /workload trainer={} action={} date={} duration={}",
                request.getTrainerUsername(),
                request.getActionType(),
                request.getTrainingDate(),
                request.getTrainingDuration());
        workloadService.applyTrainingEvent(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns monthly durations for a trainer.
     * Optional {@code year} / {@code month} narrow the result.
     */
    @GetMapping("/{trainerUsername}")
    public ResponseEntity<TrainerWorkloadResponse> getWorkload(
            @PathVariable String trainerUsername,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        log.info("GET /workload/{} year={} month={}", trainerUsername, year, month);
        if (month != null && year == null) {
            throw new IllegalArgumentException("year is required when month is provided");
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        return ResponseEntity.ok(workloadService.getWorkload(trainerUsername, year, month));
    }

    /**
     * Convenience endpoint: training summary duration for one trainer / year / month.
     */
    @GetMapping("/{trainerUsername}/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthHours(
            @PathVariable String trainerUsername,
            @PathVariable int year,
            @PathVariable int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        int duration = workloadService.getMonthDuration(trainerUsername, year, month);
        return ResponseEntity.ok(Map.of(
                "trainerUsername", trainerUsername,
                "year", year,
                "month", month,
                "trainingSummaryDuration", duration
        ));
    }
}
