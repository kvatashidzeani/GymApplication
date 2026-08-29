package com.gymcrm.workload.messaging;

import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Forwards invalid workload JMS messages to the dead letter queue for later inspection.
 */
@Component
public class WorkloadDeadLetterPublisher {

    private static final Logger log = LoggerFactory.getLogger(WorkloadDeadLetterPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final String deadLetterQueue;
    private final String sourceQueue;

    public WorkloadDeadLetterPublisher(
            JmsTemplate jmsTemplate,
            @Value("${gymcrm.workload.dlq:" + WorkloadMessaging.DEAD_LETTER_QUEUE + "}") String deadLetterQueue,
            @Value("${gymcrm.workload.queue:" + WorkloadMessaging.QUEUE + "}") String sourceQueue) {
        this.jmsTemplate = jmsTemplate;
        this.deadLetterQueue = deadLetterQueue;
        this.sourceQueue = sourceQueue;
    }

    public void publish(WorkloadUpdateRequest payload,
                        String reason,
                        String transactionId,
                        String authorization) {
        Object body = payload != null ? payload : Collections.emptyMap();
        jmsTemplate.convertAndSend(deadLetterQueue, body, message -> {
            message.setStringProperty(WorkloadMessaging.DLQ_REASON_HEADER, reason);
            message.setStringProperty(WorkloadMessaging.ORIGINAL_QUEUE_HEADER, sourceQueue);
            if (transactionId != null && !transactionId.isBlank()) {
                message.setStringProperty(WorkloadMessaging.TRANSACTION_ID_HEADER, transactionId);
            }
            if (authorization != null && !authorization.isBlank()) {
                message.setStringProperty(WorkloadMessaging.AUTHORIZATION_HEADER, authorization);
            }
            return message;
        });

        log.warn("Forwarded invalid workload message to DLQ queue={} reason={} trainer={}",
                deadLetterQueue, reason, payload != null ? payload.getTrainerUsername() : "?");
    }
}
