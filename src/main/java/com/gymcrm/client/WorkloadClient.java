package com.gymcrm.client;

import com.gymcrm.logging.TransactionContext;
import com.gymcrm.messaging.WorkloadMessaging;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.User;
import com.gymcrm.security.JwtTokenHolder;
import com.gymcrm.security.ServiceJwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes trainer workload events to ActiveMQ (asynchronous microservice communication).
 * Replaces synchronous REST calls to trainer-workload-service.
 */
@Component
public class WorkloadClient {

    public static final String CIRCUIT_BREAKER_NAME = "workloadService";

    private static final Logger log = LoggerFactory.getLogger(WorkloadClient.class);

    private final ObjectProvider<JmsTemplate> jmsTemplateProvider;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final ServiceJwtProvider serviceJwtProvider;
    private final boolean enabled;
    private final String queueName;

    public WorkloadClient(
            ObjectProvider<JmsTemplate> jmsTemplateProvider,
            ObjectProvider<CircuitBreakerFactory<?, ?>> circuitBreakerFactoryProvider,
            ObjectProvider<ServiceJwtProvider> serviceJwtProviderProvider,
            @Value("${gymcrm.workload.enabled:true}") boolean enabled,
            @Value("${gymcrm.workload.queue:" + WorkloadMessaging.QUEUE + "}") String queueName) {
        this.jmsTemplateProvider = jmsTemplateProvider;
        this.circuitBreakerFactory = circuitBreakerFactoryProvider.getIfAvailable();
        this.serviceJwtProvider = serviceJwtProviderProvider.getIfAvailable();
        this.enabled = enabled;
        this.queueName = queueName;
    }

    public void notifyTrainingAdded(Trainer trainer, Training training) {
        notify(trainer, training, WorkloadActionType.ADD);
    }

    public void notifyTrainingDeleted(Trainer trainer, Training training) {
        notify(trainer, training, WorkloadActionType.DELETE);
    }

    private void notify(Trainer trainer, Training training, WorkloadActionType actionType) {
        if (!enabled) {
            log.debug("Workload client disabled; skip {} for training id={}",
                    actionType, training != null ? training.getId() : null);
            return;
        }
        if (trainer == null || trainer.getUser() == null || training == null) {
            log.warn("Cannot publish workload event ({}): missing trainer/user/training", actionType);
            return;
        }

        JmsTemplate jmsTemplate = jmsTemplateProvider.getIfAvailable();
        if (jmsTemplate == null) {
            log.warn("JmsTemplate not available; skip workload {} for training id={}",
                    actionType, training.getId());
            return;
        }

        User user = trainer.getUser();
        WorkloadUpdateRequest message = new WorkloadUpdateRequest(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );

        if (circuitBreakerFactory != null) {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_NAME);
            circuitBreaker.run(
                    () -> publish(jmsTemplate, message, user.getUsername(), actionType),
                    throwable -> fallback(actionType, user.getUsername(), throwable));
        } else {
            try {
                publish(jmsTemplate, message, user.getUsername(), actionType);
            } catch (RuntimeException ex) {
                fallback(actionType, user.getUsername(), ex);
            }
        }
    }

    private Boolean publish(JmsTemplate jmsTemplate,
                            WorkloadUpdateRequest message,
                            String trainerUsername,
                            WorkloadActionType actionType) {
        String transactionId = TransactionContext.get();
        String bearer = resolveBearerToken();

        jmsTemplate.convertAndSend(queueName, message, jmsMessage -> {
            if (transactionId != null && !transactionId.isBlank()) {
                jmsMessage.setStringProperty(WorkloadMessaging.TRANSACTION_ID_HEADER, transactionId);
            }
            if (bearer != null && !bearer.isBlank()) {
                jmsMessage.setStringProperty(WorkloadMessaging.AUTHORIZATION_HEADER, "Bearer " + bearer);
            }
            return jmsMessage;
        });

        log.info("Published workload {} to queue={} trainer={} trainingDate={} duration={} transactionId={}",
                actionType, queueName, trainerUsername, message.getTrainingDate(),
                message.getTrainingDuration(), transactionId);
        return Boolean.TRUE;
    }

    private String resolveBearerToken() {
        String requestToken = JwtTokenHolder.get();
        if (requestToken != null && !requestToken.isBlank()) {
            return requestToken;
        }
        if (serviceJwtProvider != null) {
            return serviceJwtProvider.getToken();
        }
        return null;
    }

    private Boolean fallback(WorkloadActionType actionType,
                             String trainerUsername,
                             Throwable throwable) {
        log.warn("Circuit breaker fallback for workload {} queue={}: trainer={}, reason={}",
                actionType, queueName, trainerUsername,
                throwable != null ? throwable.getMessage() : "open circuit");
        return Boolean.FALSE;
    }
}
