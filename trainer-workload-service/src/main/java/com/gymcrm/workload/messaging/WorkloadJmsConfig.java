package com.gymcrm.workload.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class WorkloadJmsConfig {

    @Bean
    @ConditionalOnMissingBean(ConnectionFactory.class)
    public ConnectionFactory activeMqConnectionFactory(
            @Value("${spring.activemq.broker-url:tcp://localhost:61616}") String brokerUrl,
            @Value("${spring.activemq.user:}") String user,
            @Value("${spring.activemq.password:}") String password) {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        if (user != null && !user.isBlank()) {
            factory.setUserName(user);
            factory.setPassword(password);
        }
        return factory;
    }

    @Bean
    public MessageConverter workloadJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    @Bean
    public JmsTemplate workloadJmsTemplate(ConnectionFactory connectionFactory,
                                           MessageConverter workloadJmsMessageConverter) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(workloadJmsMessageConverter);
        template.setPubSubDomain(false);
        return template;
    }
}
