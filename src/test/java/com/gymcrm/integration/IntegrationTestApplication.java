package com.gymcrm.integration;

import com.gymcrm.GymApplication;
import com.gymcrm.GymRestApplication;
import com.gymcrm.config.ConsoleConfig;
import com.gymcrm.config.InMemoryTestConfig;
import com.gymcrm.messaging.WorkloadJmsConfig;
import com.gymcrm.workload.WorkloadApplication;
import com.gymcrm.workload.config.WorkloadSecurityConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Combined Gym CRM + trainer-workload-service context for cross-microservice integration tests.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableMongoRepositories(basePackages = "com.gymcrm.workload.repository")
@ComponentScan(
        basePackages = {"com.gymcrm", "com.gymcrm.workload"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.gymcrm\\.component\\..*"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        GymRestApplication.class,
                        WorkloadApplication.class,
                        GymApplication.class,
                        ConsoleConfig.class,
                        InMemoryTestConfig.class,
                        WorkloadJmsConfig.class,
                        com.gymcrm.workload.messaging.WorkloadJmsConfig.class,
                        WorkloadSecurityConfig.class,
                        com.gymcrm.workload.logging.OperationLoggingAspect.class,
                        com.gymcrm.workload.logging.TransactionLoggingFilter.class
                })
        })
@EnableDiscoveryClient
public class IntegrationTestApplication {
}
