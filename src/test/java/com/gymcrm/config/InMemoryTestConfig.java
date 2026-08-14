package com.gymcrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Test alias for {@link ConsoleConfig} (in-memory context without web/JPA Boot entry).
 */
@Configuration
@Import(ConsoleConfig.class)
public class InMemoryTestConfig {
}
