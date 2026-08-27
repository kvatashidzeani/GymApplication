package com.gymcrm.client;

import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.User;
import com.gymcrm.logging.TransactionContext;
import com.gymcrm.security.JwtTokenHolder;
import com.gymcrm.security.ServiceJwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client that notifies the trainer-workload microservice when a training
 * is added or deleted in Gym CRM.
 * <p>
 * Sends {@code Authorization: Bearer &lt;JWT&gt;} (forwarded user token when present,
 * otherwise a Gym CRM service token). Protected by a Resilience4j circuit breaker.
 */
@Component
public class WorkloadClient {

    public static final String CIRCUIT_BREAKER_NAME = "workloadService";

    private static final Logger log = LoggerFactory.getLogger(WorkloadClient.class);

    private final RestTemplate restTemplate;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final ServiceJwtProvider serviceJwtProvider;
    private final boolean enabled;
    private final boolean useDiscovery;
    private final String serviceName;
    private final String baseUrl;

    public WorkloadClient(
            RestTemplate restTemplate,
            ObjectProvider<CircuitBreakerFactory<?, ?>> circuitBreakerFactoryProvider,
            ObjectProvider<ServiceJwtProvider> serviceJwtProviderProvider,
            @Value("${gymcrm.workload.enabled:true}") boolean enabled,
            @Value("${gymcrm.workload.use-discovery:false}") boolean useDiscovery,
            @Value("${gymcrm.workload.service-name:trainer-workload-service}") String serviceName,
            @Value("${gymcrm.workload.base-url:http://localhost:8082}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.circuitBreakerFactory = circuitBreakerFactoryProvider.getIfAvailable();
        this.serviceJwtProvider = serviceJwtProviderProvider.getIfAvailable();
        this.enabled = enabled;
        this.useDiscovery = useDiscovery;
        this.serviceName = serviceName;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /** Test-friendly constructor. */
    WorkloadClient(
            RestTemplate restTemplate,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory,
            ServiceJwtProvider serviceJwtProvider,
            boolean enabled,
            boolean useDiscovery,
            String serviceName,
            String baseUrl) {
        this.restTemplate = restTemplate;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.serviceJwtProvider = serviceJwtProvider;
        this.enabled = enabled;
        this.useDiscovery = useDiscovery;
        this.serviceName = serviceName;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public void notifyTrainingAdded(Trainer trainer, Training training) {
        notify(trainer, training, WorkloadActionType.ADD);
    }

    public void notifyTrainingDeleted(Trainer trainer, Training training) {
        notify(trainer, training, WorkloadActionType.DELETE);
    }

    private String workloadUrl() {
        if (useDiscovery) {
            return "http://" + serviceName + "/workload";
        }
        return baseUrl + "/workload";
    }

    private void notify(Trainer trainer, Training training, WorkloadActionType actionType) {
        if (!enabled) {
            log.debug("Workload client disabled; skip {} for training id={}",
                    actionType, training != null ? training.getId() : null);
            return;
        }
        if (trainer == null || trainer.getUser() == null || training == null) {
            log.warn("Cannot notify workload service ({}): missing trainer/user/training", actionType);
            return;
        }

        User user = trainer.getUser();
        WorkloadUpdateRequest body = new WorkloadUpdateRequest(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );

        String url = workloadUrl();
        if (circuitBreakerFactory != null) {
            CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_NAME);
            circuitBreaker.run(
                    () -> postWorkloadUpdate(url, body, user.getUsername(), actionType),
                    throwable -> fallback(actionType, url, user.getUsername(), throwable));
        } else {
            try {
                postWorkloadUpdate(url, body, user.getUsername(), actionType);
            } catch (RuntimeException ex) {
                fallback(actionType, url, user.getUsername(), ex);
            }
        }
    }

    private Boolean postWorkloadUpdate(String url,
                                       WorkloadUpdateRequest body,
                                       String trainerUsername,
                                       WorkloadActionType actionType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String bearer = resolveBearerToken();
            if (bearer != null && !bearer.isBlank()) {
                headers.setBearerAuth(bearer);
            } else {
                log.warn("No JWT available for workload call to {}", url);
            }
            String transactionId = TransactionContext.get();
            if (transactionId != null && !transactionId.isBlank()) {
                headers.set(TransactionContext.HEADER, transactionId);
            }
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, headers), Void.class);
            log.info("Notified workload service {} via {} for trainer={} trainingDate={} duration={} transactionId={} → HTTP {}",
                    actionType, url, trainerUsername, body.getTrainingDate(),
                    body.getTrainingDuration(), transactionId, response.getStatusCode().value());
            return Boolean.TRUE;
        } catch (RestClientException ex) {
            log.error("Failed to notify workload service ({} {}) for trainer={}: {}",
                    actionType, url, trainerUsername, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Prefer the caller's Bearer token; otherwise mint/cache a Gym CRM service JWT.
     */
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
                             String url,
                             String trainerUsername,
                             Throwable throwable) {
        log.warn("Circuit breaker fallback for workload {} ({}): trainer={}, reason={}",
                actionType, url, trainerUsername,
                throwable != null ? throwable.getMessage() : "open circuit");
        return Boolean.FALSE;
    }
}
