package com.gymcrm.workload.messaging;

import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.logging.TransactionContext;
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
 * Consumes workload events from ActiveMQ and updates the in-memory monthly summary.
 * Invalid messages (missing required fields or auth) are forwarded to the DLQ.
 */
@Component
public class WorkloadMessageListener {

    private static final Logger log = LoggerFactory.getLogger(WorkloadMessageListener.class);

    private final WorkloadService workloadService;
    private final WorkloadJwtService jwtService;
    private final WorkloadMessageValidator messageValidator;
    private final WorkloadDeadLetterPublisher deadLetterPublisher;

    public WorkloadMessageListener(WorkloadService workloadService,
                                   WorkloadJwtService jwtService,
                                   WorkloadMessageValidator messageValidator,
                                   WorkloadDeadLetterPublisher deadLetterPublisher) {
        this.workloadService = workloadService;
        this.jwtService = jwtService;
        this.messageValidator = messageValidator;
        this.deadLetterPublisher = deadLetterPublisher;
    }

    @JmsListener(destination = "${gymcrm.workload.queue:workload.events.queue}")
    public void onWorkloadEvent(
            @Payload WorkloadUpdateRequest request,
            @Header(value = WorkloadMessaging.TRANSACTION_ID_HEADER, required = false) String transactionId,
            @Header(value = WorkloadMessaging.AUTHORIZATION_HEADER, required = false) String authorization) {

        if (transactionId != null && !transactionId.isBlank()) {
            TransactionContext.set(transactionId);
        }

        try {
            List<String> validationErrors = messageValidator.validate(request);
            if (!validationErrors.isEmpty()) {
                String reason = validationErrors.stream().collect(Collectors.joining("; "));
                deadLetterPublisher.publish(request, reason, transactionId, authorization);
                return;
            }

            validateAuthorization(authorization);

            log.info("Received workload {} from queue for trainer={} date={} transactionId={}",
                    request.getActionType(), request.getTrainerUsername(),
                    request.getTrainingDate(), transactionId);
            workloadService.applyTrainingEvent(request);
        } catch (IllegalArgumentException ex) {
            deadLetterPublisher.publish(request, ex.getMessage(), transactionId, authorization);
        } finally {
            TransactionContext.clear();
        }
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
