package com.gymcrm.integration.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.integration.IntegrationScenarioContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RegistrationSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IntegrationScenarioContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void resetContext() {
        context.reset();
    }

    @Given("a registered trainer with specialization {string}")
    public void registeredTrainer(String specialization) throws Exception {
        String payload = """
                {
                  "firstName": "Integration",
                  "lastName": "Trainer",
                  "specialization": "%s"
                }
                """.formatted(specialization);

        MvcResult result = mockMvc.perform(post("/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus(), result.getResponse().getContentAsString());
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        context.setTrainerUsername(json.path("username").asText());
    }

    @Given("a registered trainee exists")
    public void registeredTrainee() throws Exception {
        String payload = """
                {
                  "firstName": "Integration",
                  "lastName": "Trainee",
                  "dateOfBirth": "2000-03-01",
                  "address": "Integration City"
                }
                """;

        MvcResult result = mockMvc.perform(post("/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn();

        assertEquals(200, result.getResponse().getStatus(), result.getResponse().getContentAsString());
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        context.setTraineeUsername(json.path("username").asText());
        context.setTraineePassword(json.path("password").asText());
        context.setBearerToken(json.path("token").asText());
    }

    @Given("I am authenticated as the registered trainee")
    public void authenticatedAsTrainee() {
        assertNotNull(context.getBearerToken(), "Bearer token must be set by trainee registration");
    }
}
