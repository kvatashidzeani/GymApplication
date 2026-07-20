package com.gymcrm.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "com.gymcrm")
@PropertySource("classpath:application.properties")
@EnableTransactionManagement(proxyTargetClass = true)
public class AppConfig {
}
