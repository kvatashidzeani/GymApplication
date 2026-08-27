package com.gymcrm.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate beans for calling other microservices.
 * When Eureka discovery is enabled, the {@code @LoadBalanced} bean resolves
 * logical service names (e.g. {@code http://trainer-workload-service/...}).
 */
@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "gymcrm.workload.use-discovery", havingValue = "false", matchIfMissing = true)
    public RestTemplate restTemplate() {
        return createRestTemplate();
    }

    @Bean
    @Primary
    @LoadBalanced
    @ConditionalOnProperty(name = "gymcrm.workload.use-discovery", havingValue = "true")
    public RestTemplate loadBalancedRestTemplate() {
        return createRestTemplate();
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(5_000);
        return new RestTemplate(factory);
    }
}
