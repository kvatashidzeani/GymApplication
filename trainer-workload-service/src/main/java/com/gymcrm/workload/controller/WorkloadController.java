package com.gymcrm.workload.controller;

import com.gymcrm.workload.dto.TrainerWorkloadResponse;
import com.gymcrm.workload.service.WorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST API for reading trainer monthly workload.
 * Updates are received asynchronously via ActiveMQ ({@code workload.events.queue}).
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
     * Browser-friendly hint. Workload updates are published to ActiveMQ by Gym CRM.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> workloadUsage() {
        return ResponseEntity.ok(Map.of(
                "message", "Workload updates are consumed asynchronously from ActiveMQ queue 'workload.events.queue'.",
                "readEndpoints", Map.of(
                        "trainerSummary", "GET /workload/{trainerUsername}",
                        "monthHours", "GET /workload/{trainerUsername}/{year}/{month}"
                )
        ));
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
