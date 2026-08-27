package com.gymcrm.client;

import com.gymcrm.logging.TransactionContext;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.security.JwtTokenHolder;
import com.gymcrm.security.ServiceJwtProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkloadClientTest {

    private RestTemplate restTemplate;
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private CircuitBreaker circuitBreaker;
    private ServiceJwtProvider serviceJwtProvider;
    private WorkloadClient client;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        circuitBreakerFactory = mock(CircuitBreakerFactory.class);
        circuitBreaker = mock(CircuitBreaker.class);
        serviceJwtProvider = mock(ServiceJwtProvider.class);
        when(serviceJwtProvider.getToken()).thenReturn("service-jwt-token");
        when(circuitBreakerFactory.create(WorkloadClient.CIRCUIT_BREAKER_NAME)).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(inv -> {
            Supplier<?> supplier = inv.getArgument(0);
            Function<Throwable, ?> fallback = inv.getArgument(1);
            try {
                return supplier.get();
            } catch (Throwable ex) {
                return fallback.apply(ex);
            }
        });

        client = new WorkloadClient(
                restTemplate, circuitBreakerFactory, serviceJwtProvider, true, false,
                "trainer-workload-service", "http://localhost:8082");
    }

    @AfterEach
    void tearDown() {
        JwtTokenHolder.clear();
        TransactionContext.clear();
    }

    @Test
    void notifyTrainingAdded_sendsBearerServiceToken() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        client.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(restTemplate).postForEntity(
                eq("http://localhost:8082/workload"),
                argThat((HttpEntity<?> entity) -> {
                    HttpHeaders headers = entity.getHeaders();
                    return "Bearer service-jwt-token".equals(headers.getFirst(HttpHeaders.AUTHORIZATION));
                }),
                eq(Void.class));
    }

    @Test
    void notifyTrainingAdded_propagatesTransactionIdHeader() {
        TransactionContext.set("tx-from-gym-crm");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        client.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(restTemplate).postForEntity(
                eq("http://localhost:8082/workload"),
                argThat((HttpEntity<?> entity) ->
                        "tx-from-gym-crm".equals(entity.getHeaders().getFirst(TransactionContext.HEADER))),
                eq(Void.class));
    }

    @Test
    void notifyTrainingAdded_forwardsUserBearerTokenWhenPresent() {
        JwtTokenHolder.set("user-jwt-token");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        client.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(restTemplate).postForEntity(
                eq("http://localhost:8082/workload"),
                argThat((HttpEntity<?> entity) ->
                        "Bearer user-jwt-token".equals(entity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))),
                eq(Void.class));
        verify(serviceJwtProvider, never()).getToken();
    }

    @Test
    void notifyTrainingAdded_usesServiceDiscoveryUrl() {
        WorkloadClient discoveryClient = new WorkloadClient(
                restTemplate, circuitBreakerFactory, serviceJwtProvider, true, true,
                "trainer-workload-service", "http://localhost:8082");
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        discoveryClient.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(restTemplate).postForEntity(
                eq("http://trainer-workload-service/workload"),
                any(HttpEntity.class),
                eq(Void.class));
    }

    @Test
    void notifyTrainingDeleted_postsDeleteAction() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        client.notifyTrainingDeleted(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(restTemplate).postForEntity(
                eq("http://localhost:8082/workload"),
                any(HttpEntity.class),
                eq(Void.class));
    }

    @Test
    void whenDisabled_doesNotCallRemote() {
        WorkloadClient disabled = new WorkloadClient(
                restTemplate, circuitBreakerFactory, serviceJwtProvider, false, false,
                "trainer-workload-service", "http://localhost:8082");
        disabled.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(circuitBreakerFactory);
    }

    @Test
    void remoteFailure_usesCircuitBreakerFallback() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RestClientException("connection refused"));

        client.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(circuitBreaker).run(any(Supplier.class), any(Function.class));
    }

    @Test
    void openCircuit_invokesFallbackWithoutPropagating() {
        when(circuitBreaker.run(any(Supplier.class), any(Function.class))).thenAnswer(inv -> {
            Function<Throwable, ?> fallback = inv.getArgument(1);
            return fallback.apply(new RuntimeException("CircuitBreaker 'workloadService' is OPEN"));
        });

        client.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(restTemplate, never()).postForEntity(anyString(), any(), any());
    }

    private static Trainer trainer(String username, String first, String last, boolean active) {
        User user = new User(first, last, username, "secret", active, 20L);
        Trainer trainer = new Trainer(2L, new TrainingType("Cardio", 1L), 20L);
        trainer.setUser(user);
        return trainer;
    }

    private static Training training(LocalDate date, int duration) {
        Training training = new Training();
        training.setId(100L);
        training.setTrainerId(2L);
        training.setTrainingDate(date);
        training.setTrainingDuration(duration);
        return training;
    }
}
