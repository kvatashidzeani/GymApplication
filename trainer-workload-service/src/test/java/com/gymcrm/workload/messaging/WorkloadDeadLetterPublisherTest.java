package com.gymcrm.workload.messaging;

import com.gymcrm.workload.dto.ActionType;
import com.gymcrm.workload.dto.WorkloadUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WorkloadDeadLetterPublisherTest {

    private JmsTemplate jmsTemplate;
    private WorkloadDeadLetterPublisher publisher;

    @BeforeEach
    void setUp() {
        jmsTemplate = mock(JmsTemplate.class);
        publisher = new WorkloadDeadLetterPublisher(
                jmsTemplate, WorkloadMessaging.DEAD_LETTER_QUEUE, WorkloadMessaging.QUEUE);
    }

    @Test
    void publish_sendsToConfiguredDlq() {
        WorkloadUpdateRequest request = new WorkloadUpdateRequest();
        request.setTrainerUsername("Mike.Brown");
        request.setTrainerFirstName("Mike");
        request.setTrainerLastName("Brown");
        request.setIsActive(true);
        request.setTrainingDate(LocalDate.of(2026, 8, 29));
        request.setTrainingDuration(60);
        request.setActionType(ActionType.ADD);

        publisher.publish(request, "trainerUsername: must not be blank", "tx-1", "Bearer jwt");

        verify(jmsTemplate).convertAndSend(
                eq(WorkloadMessaging.DEAD_LETTER_QUEUE),
                eq(request),
                any());
    }
}
