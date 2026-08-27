package com.gymcrm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * Spring Boot entry point for the Gym CRM REST API.
 * <p>
 * Run this class (or {@code mvn spring-boot:run}).
 * Swagger UI: {@code http://localhost:8081/swagger-ui.html}
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GymRestApplication {

    private static final Logger log = LoggerFactory.getLogger(GymRestApplication.class);

    private final Environment environment;

    public GymRestApplication(Environment environment) {
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(GymRestApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupLinks() {
        String port = environment.getProperty("local.server.port",
                environment.getProperty("server.port", "8081"));
        String base = "http://localhost:" + port;
        String profiles = String.join(", ", environment.getActiveProfiles());
        if (profiles.isBlank()) {
            profiles = String.join(", ", environment.getDefaultProfiles());
        }
        log.info("Gym CRM REST API started (profiles: {})", profiles);
        log.info("API base:    {}", base);
        log.info("Swagger UI:  {}/swagger-ui.html", base);
        log.info("OpenAPI:     {}/v3/api-docs", base);
        log.info("Health:      {}/actuator/health", base);
        log.info("Prometheus:  {}/actuator/prometheus", base);
        log.info("Metrics:     {}/actuator/metrics", base);
        log.info("Info:        {}/actuator/info", base);
        log.info("Auth: JWT Bearer — register/login issue token; POST /logout blacklists token; Swagger is public");
    }
}
