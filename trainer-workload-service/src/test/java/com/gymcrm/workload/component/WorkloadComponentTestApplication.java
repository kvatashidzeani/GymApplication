package com.gymcrm.workload.component;

import com.gymcrm.workload.storage.InMemoryWorkloadStorage;
import com.gymcrm.workload.storage.WorkloadStorage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test-only Spring Boot entry that boots the workload REST stack without MongoDB or ActiveMQ.
 */
@SpringBootApplication(
        scanBasePackages = "com.gymcrm.workload",
        exclude = UserDetailsServiceAutoConfiguration.class)
@EnableDiscoveryClient
public class WorkloadComponentTestApplication {

    @Bean
    @Primary
    WorkloadStorage workloadStorage() {
        return new InMemoryWorkloadStorage();
    }
}
