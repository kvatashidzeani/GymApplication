package com.gymcrm.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Core application config for console and REST entry points.
 * Web MVC ({@link WebConfig}) is excluded from the scan so {@link com.gymcrm.GymApplication}
 * can start without a ServletContext; {@link com.gymcrm.GymRestApplication} registers
 * {@link WebConfig} explicitly with the web application context.
 */
@Configuration
@ComponentScan(
        basePackages = "com.gymcrm",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebConfig.class
        )
)
@PropertySource("classpath:application.properties")
@EnableTransactionManagement(proxyTargetClass = true)
public class AppConfig {
}
