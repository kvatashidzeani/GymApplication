package com.gymcrm.workload.config;

import com.gymcrm.workload.storage.InMemoryWorkloadStorage;
import com.gymcrm.workload.storage.WorkloadStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * In-memory storage when running the Report service in Docker without MongoDB.
 */
@Configuration
@Profile("docker")
public class DockerWorkloadConfig {

    @Bean
    @Primary
    WorkloadStorage workloadStorage() {
        return new InMemoryWorkloadStorage();
    }
}
