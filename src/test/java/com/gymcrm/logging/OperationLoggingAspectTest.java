package com.gymcrm.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OperationLoggingAspectTest {

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void logsSuccessfulOperationWithTransactionId() throws Throwable {
        TransactionContext.set("tx-op-1");
        OperationLoggingAspect aspect = new OperationLoggingAspect();

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) TrainingServiceStub.class);
        when(signature.getName()).thenReturn("addTraining");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"John.Doe", 60});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.logOperation(joinPoint);

        assertEquals("ok", result);
        verify(joinPoint).proceed();
    }

    @Test
    void rethrowsAndLogsError() throws Throwable {
        TransactionContext.set("tx-op-2");
        OperationLoggingAspect aspect = new OperationLoggingAspect();

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) TrainingServiceStub.class);
        when(signature.getName()).thenReturn("addTraining");
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenThrow(new IllegalArgumentException("bad input"));

        assertThrows(IllegalArgumentException.class, () -> aspect.logOperation(joinPoint));
    }

    static class TrainingServiceStub {
    }
}
