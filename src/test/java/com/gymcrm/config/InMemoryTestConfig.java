package com.gymcrm.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring context for integration tests: in-memory storage only, no Hibernate/JPA.
 * Excludes AppConfig because its nested @ComponentScan would pull Hibernate back in.
 */
@Configuration
@ComponentScan(
        basePackages = "com.gymcrm",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AppConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = HibernateConfig.class)
        }
)
@PropertySource("classpath:application.properties")
public class InMemoryTestConfig {
}
