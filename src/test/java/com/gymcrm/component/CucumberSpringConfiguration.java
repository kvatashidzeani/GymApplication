package com.gymcrm.component;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the Gym CRM REST stack for Cucumber component scenarios.
 */
@CucumberContextConfiguration
@SpringBootTest(
        classes = GymCrmComponentTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
}
