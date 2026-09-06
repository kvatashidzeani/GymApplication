package com.gymcrm.component;

import com.gymcrm.GymApplication;
import com.gymcrm.GymRestApplication;
import com.gymcrm.config.ConsoleConfig;
import com.gymcrm.config.InMemoryTestConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Gym CRM-only Spring Boot entry for component tests.
 * Excludes trainer-workload packages that appear on the test classpath for integration tests.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
@ComponentScan(
        basePackages = "com.gymcrm",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.gymcrm\\.workload\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.gymcrm\\.integration\\..*"),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        GymRestApplication.class,
                        GymApplication.class,
                        ConsoleConfig.class,
                        InMemoryTestConfig.class,
                        GymCrmComponentTestApplication.class
                })
        })
@EnableDiscoveryClient
public class GymCrmComponentTestApplication {
}
