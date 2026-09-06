package com.gymcrm.workload.component;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Boots the trainer workload REST API for Cucumber component scenarios.
 */
@CucumberContextConfiguration
@SpringBootTest(
        classes = WorkloadComponentTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.profiles.active=component-test",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "gymcrm.jwt.secret=GymCrmJwtSecretKey-ChangeMe-AtLeast32CharsLong!",
        "gymcrm.jwt.expiration-ms=3600000"
})
public class CucumberSpringConfiguration {
}
