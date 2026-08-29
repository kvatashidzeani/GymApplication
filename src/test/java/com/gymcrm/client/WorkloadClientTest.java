package com.gymcrm.client;

import com.gymcrm.logging.TransactionContext;
import com.gymcrm.messaging.WorkloadMessaging;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.security.JwtTokenHolder;
import com.gymcrm.security.ServiceJwtProvider;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkloadClientTest {

    private static final String QUEUE = WorkloadMessaging.QUEUE;

    private JmsTemplate jmsTemplate;
    @SuppressWarnings("rawtypes")
    private CircuitBreakerFactory circuitBreakerFactory;
    private CircuitBreaker circuitBreaker;
    private ServiceJwtProvider serviceJwtProvider;
    private WorkloadClient client;

    @BeforeEach
    void setUp() {
        jmsTemplate = mock(JmsTemplate.class);
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
                providerOf(jmsTemplate),
                providerOf(circuitBreakerFactory),
                providerOf(serviceJwtProvider),
                true,
                QUEUE);
    }

    @AfterEach
    void tearDown() {
        JwtTokenHolder.clear();
        TransactionContext.clear();
    }

    @Test
    void notifyTrainingAdded_sendsBearerServiceToken() throws JMSException {
        client.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        MessagePostProcessor processor = capturePostProcessor();
        Message message = mock(Message.class);
        processor.postProcessMessage(message);
        verify(message).setStringProperty(WorkloadMessaging.AUTHORIZATION_HEADER, "Bearer service-jwt-token");
        verify(jmsTemplate).convertAndSend(eq(QUEUE), any(WorkloadUpdateRequest.class), any(MessagePostProcessor.class));
    }

    @Test
    void notifyTrainingAdded_propagatesTransactionIdHeader() throws JMSException {
        TransactionContext.set("tx-from-gym-crm");

        client.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        MessagePostProcessor processor = capturePostProcessor();
        Message message = mock(Message.class);
        processor.postProcessMessage(message);
        verify(message).setStringProperty(WorkloadMessaging.TRANSACTION_ID_HEADER, "tx-from-gym-crm");
    }

    @Test
    void notifyTrainingAdded_forwardsUserBearerTokenWhenPresent() throws JMSException {
        JwtTokenHolder.set("user-jwt-token");

        client.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        MessagePostProcessor processor = capturePostProcessor();
        Message message = mock(Message.class);
        processor.postProcessMessage(message);
        verify(message).setStringProperty(WorkloadMessaging.AUTHORIZATION_HEADER, "Bearer user-jwt-token");
        verify(serviceJwtProvider, never()).getToken();
    }

    @Test
    void notifyTrainingAdded_publishesToConfiguredQueue() {
        WorkloadClient customQueueClient = new WorkloadClient(
                providerOf(jmsTemplate),
                providerOf(circuitBreakerFactory),
                providerOf(serviceJwtProvider),
                true,
                "custom.workload.queue");

        customQueueClient.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(jmsTemplate).convertAndSend(
                eq("custom.workload.queue"),
                any(WorkloadUpdateRequest.class),
                any(MessagePostProcessor.class));
    }

    @Test
    void notifyTrainingDeleted_publishesDeleteAction() {
        client.notifyTrainingDeleted(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));

        verify(jmsTemplate).convertAndSend(
                eq(QUEUE),
                argThat((WorkloadUpdateRequest request) ->
                        request.getActionType() == WorkloadActionType.DELETE),
                any(MessagePostProcessor.class));
    }

    @Test
    void whenDisabled_doesNotPublish() {
        WorkloadClient disabled = new WorkloadClient(
                providerOf(jmsTemplate),
                providerOf(circuitBreakerFactory),
                providerOf(serviceJwtProvider),
                false,
                QUEUE);
        disabled.notifyTrainingAdded(
                trainer("Mike.Brown", "Mike", "Brown", true),
                training(LocalDate.of(2026, 3, 15), 60));
        verifyNoInteractions(jmsTemplate);
        verifyNoInteractions(circuitBreakerFactory);
    }

    @Test
    void publishFailure_usesCircuitBreakerFallback() {
        doThrow(new JmsException("broker unavailable") {})
                .when(jmsTemplate)
                .convertAndSend(anyString(), any(), any(MessagePostProcessor.class));

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

        verify(jmsTemplate, never()).convertAndSend(anyString(), any(), any(MessagePostProcessor.class));
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> providerOf(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private MessagePostProcessor capturePostProcessor() {
        var captor = org.mockito.ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(QUEUE), any(WorkloadUpdateRequest.class), captor.capture());
        return captor.getValue();
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
