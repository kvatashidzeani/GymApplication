package com.gymcrm.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

/**
 * Non-web Spring context: in-memory storage, services, DAOs — no Tomcat / MVC.
 * Used by the console demo ({@link com.gymcrm.GymApplication}) and integration tests.
 */
@Configuration
@ComponentScan(
        basePackages = "com.gymcrm",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AppConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WorkloadCircuitBreakerConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.gymcrm.GymRestApplication.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                        classes = com.gymcrm.logging.TransactionLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.gymcrm\\.controller\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.gymcrm\\.actuator\\..*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.gymcrm\\.security\\..*")
        }
)
@PropertySource({"classpath:application.properties", "classpath:application-console.properties"})
public class ConsoleConfig {
}
