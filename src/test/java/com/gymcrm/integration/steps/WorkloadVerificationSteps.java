package com.gymcrm.integration.steps;

import com.gymcrm.integration.IntegrationScenarioContext;
import com.gymcrm.workload.service.WorkloadService;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that Gym CRM → ActiveMQ → workload-service updates MongoDB summaries.
 */
public class WorkloadVerificationSteps {

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private IntegrationScenarioContext context;

    @Then("the trainer workload for year {int} month {int} should be {int} minutes")
    public void trainerWorkloadShouldBe(int year, int month, int expectedMinutes) {
        if (expectedMinutes == 0) {
            assertEquals(0,
                    workloadService.getMonthDuration(context.getTrainerUsername(), year, month),
                    "Workload unexpectedly updated for trainer " + context.getTrainerUsername());
            return;
        }

        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> assertEquals(
                        expectedMinutes,
                        workloadService.getMonthDuration(context.getTrainerUsername(), year, month),
                        "Workload not updated for trainer " + context.getTrainerUsername()));
    }
}
