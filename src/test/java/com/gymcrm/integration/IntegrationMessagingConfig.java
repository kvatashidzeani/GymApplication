package com.gymcrm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.messaging.WorkloadMessaging;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shared embedded ActiveMQ config for integration tests.
 * Maps Gym CRM and workload WorkloadUpdateRequest type ids onto the consumer DTO.
 */
@Configuration
@EnableJms
@Profile("integration-test")
public class IntegrationMessagingConfig {

    private static final String BROKER_NAME = "gymIntegrationBroker-" + UUID.randomUUID();
    private static final String BROKER_URL = "vm://" + BROKER_NAME + "?create=false";

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService integrationBroker() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName(BROKER_NAME);
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.setUseShutdownHook(false);
        broker.setAdvisorySupport(false);
        return broker;
    }

    @Bean
    @Primary
    @DependsOn("integrationBroker")
    public ConnectionFactory activeMqConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    @Primary
    public MessageConverter workloadJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("WorkloadUpdateRequest", WorkloadUpdateRequest.class);
        typeIdMappings.put("com.gymcrm.client.WorkloadUpdateRequest", WorkloadUpdateRequest.class);
        typeIdMappings.put("com.gymcrm.workload.dto.WorkloadUpdateRequest", WorkloadUpdateRequest.class);
        converter.setTypeIdMappings(typeIdMappings);
        return converter;
    }

    @Bean
    @Primary
    public JmsTemplate workloadJmsTemplate(ConnectionFactory connectionFactory,
                                           MessageConverter workloadJmsMessageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(workloadJmsMessageConverter);
        template.setPubSubDomain(false);
        template.setDefaultDestinationName(WorkloadMessaging.QUEUE);
        return template;
    }
}
