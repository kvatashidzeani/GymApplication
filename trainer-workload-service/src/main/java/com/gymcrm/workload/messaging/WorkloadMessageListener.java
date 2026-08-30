package com.gymcrm.workload.messaging;

import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.logging.JmsTransactionLogging;
import com.gymcrm.workload.security.WorkloadJwtService;
import com.gymcrm.workload.service.WorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Consumes workload events from ActiveMQ and updates the MongoDB monthly summary.
 * Invalid messages (missing required fields or auth) are forwarded to the DLQ.
 */
@Component
public class WorkloadMessageListener {

    private static final Logger log = LoggerFactory.getLogger(WorkloadMessageListener.class);
    private static final String JMS_OPERATION = "JMS " + WorkloadMessaging.QUEUE;

    private final WorkloadService workloadService;
    private final WorkloadJwtService jwtService;
    private final WorkloadMessageValidator messageValidator;
    private final WorkloadDeadLetterPublisher deadLetterPublisher;
    private final JmsTransactionLogging jmsTransactionLogging;

    public WorkloadMessageListener(WorkloadService workloadService,
                                   WorkloadJwtService jwtService,
                                   WorkloadMessageValidator messageValidator,
                                   WorkloadDeadLetterPublisher deadLetterPublisher,
                                   JmsTransactionLogging jmsTransactionLogging) {
        this.workloadService = workloadService;
        this.jwtService = jwtService;
        this.messageValidator = messageValidator;
        this.deadLetterPublisher = deadLetterPublisher;
        this.jmsTransactionLogging = jmsTransactionLogging;
    }

    @JmsListener(destination = "${gymcrm.workload.queue:workload.events.queue}")
    public void onWorkloadEvent(
            @Payload WorkloadUpdateRequest request,
            @Header(value = WorkloadMessaging.TRANSACTION_ID_HEADER, required = false) String transactionId,
            @Header(value = WorkloadMessaging.AUTHORIZATION_HEADER, required = false) String authorization) {

        jmsTransactionLogging.execute(transactionId, JMS_OPERATION, () -> {
            List<String> validationErrors = messageValidator.validate(request);
            if (!validationErrors.isEmpty()) {
                String reason = validationErrors.stream().collect(Collectors.joining("; "));
                deadLetterPublisher.publish(request, reason, transactionId, authorization);
                return JmsTransactionLogging.dlqDetail(reason);
            }

            try {
                validateAuthorization(authorization);
            } catch (IllegalArgumentException ex) {
                deadLetterPublisher.publish(request, ex.getMessage(), transactionId, authorization);
                return JmsTransactionLogging.dlqDetail(ex.getMessage());
            }

            log.info("Received workload {} from queue for trainer={} date={} transactionId={}",
                    request.getActionType(), request.getTrainerUsername(),
                    request.getTrainingDate(), transactionId);
            workloadService.applyTrainingEvent(request);
            return "processed";
        });
    }

    private void validateAuthorization(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new IllegalArgumentException("authorization: Bearer JWT is required");
        }
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7).trim() : authorization.trim();
        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("authorization: invalid or expired JWT");
        }
    }
}
