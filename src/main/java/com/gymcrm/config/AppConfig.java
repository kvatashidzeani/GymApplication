package com.gymcrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Shared non-web configuration. Component scanning is handled by
 * {@link org.springframework.boot.autoconfigure.SpringBootApplication}.
 * DataSource / JPA are provided by Spring Boot auto-configuration.
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class AppConfig {
}
