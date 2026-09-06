package com.gymcrm.workload.component.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.workload.component.ScenarioContext;
import com.gymcrm.workload.component.TestJwtFactory;
import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.service.WorkloadService;
import com.gymcrm.workload.storage.InMemoryWorkloadStorage;
import com.gymcrm.workload.storage.WorkloadStorage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class WorkloadApiSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private WorkloadStorage workloadStorage;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${gymcrm.jwt.secret}")
    private String jwtSecret;

    @Value("${gymcrm.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Given("trainer {string} has workload for year {int} month {int} with {int} minutes")
    public void trainerHasWorkload(String trainerUsername, int year, int month, int minutes) {
        if (workloadStorage instanceof InMemoryWorkloadStorage inMemory) {
            inMemory.clear();
        }
        WorkloadUpdateRequest request = new WorkloadUpdateRequest();
        request.setTrainerUsername(trainerUsername);
        request.setTrainerFirstName(trainerUsername.split("\\.")[0]);
        request.setTrainerLastName(trainerUsername.split("\\.")[1]);
        request.setIsActive(true);
        request.setActionType(ActionType.ADD);
        request.setTrainingDate(LocalDate.of(year, month, 15));
        request.setTrainingDuration(minutes);
        workloadService.applyTrainingEvent(request);
    }

    @Given("I have a valid JWT for user {string}")
    public void validJwtForUser(String username) {
        context.setBearerToken(TestJwtFactory.bearerToken(jwtSecret, username, jwtExpirationMs));
    }

    @When("I request workload for trainer {string} without authentication")
    public void requestWorkloadWithoutAuth(String trainerUsername) throws Exception {
        MvcResult result = mockMvc.perform(get("/workload/" + trainerUsername))
                .andReturn();
        storeResult(result);
    }

    @When("I request workload for trainer {string}")
    public void requestWorkload(String trainerUsername) throws Exception {
        MvcResult result = mockMvc.perform(get("/workload/" + trainerUsername)
                        .header("Authorization", "Bearer " + context.getBearerToken()))
                .andReturn();
        storeResult(result);
    }

    @When("I request month hours for trainer {string} year {int} month {int}")
    public void requestMonthHours(String trainerUsername, int year, int month) throws Exception {
        MvcResult result = mockMvc.perform(get(
                        "/workload/" + trainerUsername + "/" + year + "/" + month)
                        .header("Authorization", "Bearer " + context.getBearerToken()))
                .andReturn();
        storeResult(result);
    }

    @Then("the workload response status should be {int}")
    public void workloadResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastStatus());
    }

    @Then("the month hours response status should be {int}")
    public void monthHoursResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastStatus());
    }

    @And("the workload summary contains year {int} month {int} with duration {int}")
    public void workloadSummaryContains(int year, int month, int duration) throws Exception {
        JsonNode json = objectMapper.readTree(context.getLastBody());
        boolean found = false;
        for (JsonNode yearNode : json.path("years")) {
            if (yearNode.path("year").asInt() != year) {
                continue;
            }
            for (JsonNode monthNode : yearNode.path("months")) {
                if (monthNode.path("month").asInt() == month
                        && monthNode.path("trainingSummaryDuration").asInt() == duration) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "Expected year=" + year + " month=" + month + " duration=" + duration);
    }

    @And("the month hours duration should be {int}")
    public void monthHoursDurationShouldBe(int expectedDuration) throws Exception {
        JsonNode json = objectMapper.readTree(context.getLastBody());
        assertEquals(expectedDuration, json.path("trainingSummaryDuration").asInt());
    }

    private void storeResult(MvcResult result) throws Exception {
        context.setLastStatus(result.getResponse().getStatus());
        context.setLastBody(result.getResponse().getContentAsString());
    }
}
