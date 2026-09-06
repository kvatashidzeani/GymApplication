package com.gymcrm.component.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.component.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AuthSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScenarioContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @When("I register a trainee with first name {string} and last name {string}")
    public void registerTrainee(String firstName, String lastName) throws Exception {
        String payload = """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "dateOfBirth": "2000-01-15",
                  "address": "Component Test City"
                }
                """.formatted(firstName, lastName);

        MvcResult result = mockMvc.perform(post("/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn();

        context.setLastStatus(result.getResponse().getStatus());
        context.setLastBody(result.getResponse().getContentAsString());

        if (result.getResponse().getStatus() == 200) {
            JsonNode json = objectMapper.readTree(context.getLastBody());
            context.setRegisteredUsername(json.path("username").asText());
            context.setRegisteredPassword(json.path("password").asText());
            context.setBearerToken(json.path("token").asText());
        }
    }

    @Given("a registered trainee exists")
    public void registeredTraineeExists() throws Exception {
        registerTrainee("Component", "Trainee");
    }

    @Given("I am authenticated as the registered trainee")
    public void authenticatedAsRegisteredTrainee() {
        assertNotNull(context.getBearerToken(), "Bearer token must be set by registration");
    }

    @When("I login with the registered trainee credentials")
    public void loginWithRegisteredCredentials() throws Exception {
        loginWithCredentials(context.getRegisteredUsername(), context.getRegisteredPassword());
    }

    @When("I login with username {string} and password {string}")
    public void loginWithCredentials(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(get("/login")
                        .param("username", username)
                        .param("password", password))
                .andReturn();

        context.setLastStatus(result.getResponse().getStatus());
        context.setLastBody(result.getResponse().getContentAsString());

        if (result.getResponse().getStatus() == 200) {
            JsonNode json = objectMapper.readTree(context.getLastBody());
            context.setBearerToken(json.path("token").asText());
        }
    }

    @When("I login with username of the registered trainee and password {string}")
    public void loginWithRegisteredUsernameAndPassword(String password) throws Exception {
        loginWithCredentials(context.getRegisteredUsername(), password);
    }

    @Then("the registration response status should be {int}")
    public void registrationResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastStatus());
    }

    @Then("the login response status should be {int}")
    public void loginResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastStatus());
    }

    @And("the response should contain a username and password")
    public void responseContainsUsernameAndPassword() throws Exception {
        JsonNode json = objectMapper.readTree(context.getLastBody());
        assertFalse(json.path("username").asText().isBlank());
        assertEquals(10, json.path("password").asText().length());
    }

    @And("the response should contain a Bearer token")
    public void responseContainsBearerToken() throws Exception {
        JsonNode json = objectMapper.readTree(context.getLastBody());
        assertFalse(json.path("token").asText().isBlank());
        assertEquals("Bearer", json.path("type").asText());
    }
}
