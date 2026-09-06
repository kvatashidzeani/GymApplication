package com.gymcrm.integration.steps;

import com.gymcrm.integration.IntegrationScenarioContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TrainingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IntegrationScenarioContext context;

    @When("I add a training with the registered trainer on {string} for {int} minutes")
    public void addTraining(String trainingDate, int durationMinutes) throws Exception {
        performAddTraining(context.getTrainerUsername(), trainingDate, durationMinutes, true);
    }

    @When("I add a training with the registered trainer on {string} for {int} minutes without authentication")
    public void addTrainingWithoutAuthentication(String trainingDate, int durationMinutes) throws Exception {
        performAddTraining(context.getTrainerUsername(), trainingDate, durationMinutes, false);
    }

    @When("I add a training with trainer {string} on {string} for {int} minutes")
    public void addTrainingWithNamedTrainer(String trainerUsername, String trainingDate, int durationMinutes)
            throws Exception {
        performAddTraining(trainerUsername, trainingDate, durationMinutes, true);
    }

    @Then("the training response status should be {int}")
    public void trainingResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastStatus(), context.getLastBody());
    }

    private void performAddTraining(String trainerUsername,
                                    String trainingDate,
                                    int durationMinutes,
                                    boolean authenticated) throws Exception {
        String payload = """
                {
                  "traineeUsername": "%s",
                  "trainerUsername": "%s",
                  "trainingName": "Integration Session",
                  "trainingDate": "%s",
                  "trainingDuration": %d
                }
                """.formatted(
                context.getTraineeUsername(),
                trainerUsername,
                trainingDate,
                durationMinutes);

        MockHttpServletRequestBuilder request = post("/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);
        if (authenticated) {
            request.header("Authorization", "Bearer " + context.getBearerToken());
        }

        MvcResult result = mockMvc.perform(request).andReturn();
        context.setLastStatus(result.getResponse().getStatus());
        context.setLastBody(result.getResponse().getContentAsString());
    }
}
