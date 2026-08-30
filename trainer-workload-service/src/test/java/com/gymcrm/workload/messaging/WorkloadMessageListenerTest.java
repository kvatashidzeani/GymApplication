package com.gymcrm.workload.messaging;

import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import com.gymcrm.workload.logging.JmsTransactionLogging;
import com.gymcrm.workload.logging.TransactionContext;
import com.gymcrm.workload.security.WorkloadJwtService;
import com.gymcrm.workload.service.WorkloadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WorkloadMessageListenerTest {

    private WorkloadService workloadService;
    private WorkloadJwtService jwtService;
    private WorkloadMessageValidator messageValidator;
    private WorkloadDeadLetterPublisher deadLetterPublisher;
    private WorkloadMessageListener listener;

    @BeforeEach
    void setUp() {
        workloadService = mock(WorkloadService.class);
        jwtService = mock(WorkloadJwtService.class);
        messageValidator = mock(WorkloadMessageValidator.class);
        deadLetterPublisher = mock(WorkloadDeadLetterPublisher.class);
        listener = new WorkloadMessageListener(
                workloadService, jwtService, messageValidator, deadLetterPublisher, new JmsTransactionLogging());
    }

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void onWorkloadEvent_validMessage_appliesEvent() {
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        WorkloadUpdateRequest request = sampleRequest(ActionType.ADD);
        when(messageValidator.validate(request)).thenReturn(List.of());
        AtomicReference<String> txDuringService = new AtomicReference<>();

        doAnswer(inv -> {
            txDuringService.set(TransactionContext.get());
            return null;
        }).when(workloadService).applyTrainingEvent(request);

        listener.onWorkloadEvent(request, "tx-123", "Bearer valid-token");

        verify(workloadService).applyTrainingEvent(request);
        verify(deadLetterPublisher, never()).publish(any(), any(), any(), any());
        assertEquals("tx-123", txDuringService.get());
        assertNull(TransactionContext.get());
    }

    @Test
    void onWorkloadEvent_missingRequiredFields_sendsToDlq() {
        WorkloadUpdateRequest request = sampleRequest(ActionType.ADD);
        when(messageValidator.validate(request)).thenReturn(List.of("trainerUsername: must not be blank"));

        listener.onWorkloadEvent(request, "tx-123", "Bearer valid-token");

        verify(deadLetterPublisher).publish(
                eq(request),
                eq("trainerUsername: must not be blank"),
                eq("tx-123"),
                eq("Bearer valid-token"));
        verify(workloadService, never()).applyTrainingEvent(any());
        verify(jwtService, never()).isTokenValid(any());
        assertNull(TransactionContext.get());
    }

    @Test
    void onWorkloadEvent_missingAuthorization_sendsToDlq() {
        WorkloadUpdateRequest request = sampleRequest(ActionType.ADD);
        when(messageValidator.validate(request)).thenReturn(List.of());

        listener.onWorkloadEvent(request, "tx-123", null);

        verify(deadLetterPublisher).publish(
                eq(request),
                eq("authorization: Bearer JWT is required"),
                eq("tx-123"),
                isNull());
        verify(workloadService, never()).applyTrainingEvent(any());
        assertNull(TransactionContext.get());
    }

    @Test
    void onWorkloadEvent_invalidJwt_sendsToDlq() {
        when(jwtService.isTokenValid("bad-token")).thenReturn(false);
        WorkloadUpdateRequest request = sampleRequest(ActionType.DELETE);
        when(messageValidator.validate(request)).thenReturn(List.of());

        listener.onWorkloadEvent(request, null, "Bearer bad-token");

        verify(deadLetterPublisher).publish(
                eq(request),
                eq("authorization: invalid or expired JWT"),
                isNull(),
                eq("Bearer bad-token"));
        verify(workloadService, never()).applyTrainingEvent(any());
        assertNull(TransactionContext.get());
    }

    private static WorkloadUpdateRequest sampleRequest(ActionType actionType) {
        WorkloadUpdateRequest request = new WorkloadUpdateRequest();
        request.setTrainerUsername("Mike.Brown");
        request.setTrainerFirstName("Mike");
        request.setTrainerLastName("Brown");
        request.setIsActive(true);
        request.setTrainingDate(LocalDate.of(2026, 3, 15));
        request.setTrainingDuration(60);
        request.setActionType(actionType);
        return request;
    }
}
