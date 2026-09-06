package com.gymcrm.component.steps;

import com.gymcrm.component.ScenarioContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommonSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScenarioContext context;

    @Before
    public void resetContext() {
        context.reset();
    }

    @When("I request the health endpoint")
    public void requestHealthEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andReturn();
        context.setLastStatus(result.getResponse().getStatus());
        context.setLastBody(result.getResponse().getContentAsString());
    }

    @Then("the health response status should be {int}")
    public void healthResponseStatusShouldBe(int expectedStatus) {
        assertEquals(expectedStatus, context.getLastStatus());
    }
}
