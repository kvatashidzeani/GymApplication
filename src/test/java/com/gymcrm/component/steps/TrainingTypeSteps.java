package com.gymcrm.component.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.component.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class TrainingTypeSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @When("I request the training types catalog")
    public void requestTrainingTypes() throws Exception {
        MvcResult result = mockMvc.perform(get("/training-types")
                        .header("Authorization", "Bearer " + context.getBearerToken()))
                .andReturn();
        context.setLastStatus(result.getResponse().getStatus());
        context.setLastBody(result.getResponse().getContentAsString());
    }

    @When("I request the training types catalog without authentication")
    public void requestTrainingTypesWithoutAuthentication() throws Exception {
        MvcResult result = mockMvc.perform(get("/training-types"))
                .andReturn();
        context.setLastStatus(result.getResponse().getStatus());
        context.setLastBody(result.getResponse().getContentAsString());
    }

    @Then("the training types response status should be {int}")
    public void trainingTypesResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastStatus());
    }

    @And("the training types list should not be empty")
    public void trainingTypesListShouldNotBeEmpty() throws Exception {
        JsonNode json = objectMapper.readTree(context.getLastBody());
        assertTrue(json.isArray() && json.size() > 0);
    }
}
