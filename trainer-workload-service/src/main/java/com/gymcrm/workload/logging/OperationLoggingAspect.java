package com.gymcrm.workload.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class OperationLoggingAspect {

    private static final Logger OP_LOG = LoggerFactory.getLogger("com.gymcrm.workload.logging.Operation");

    @Around("execution(* com.gymcrm.workload.service..*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String operation = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "(", ")"));
        String txId = TransactionContext.get();

        OP_LOG.info("Operation started transactionId={} operation={} args={}", txId, operation, args);
        long started = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            OP_LOG.info("Operation finished transactionId={} operation={} status=OK durationMs={}",
                    txId, operation, System.currentTimeMillis() - started);
            return result;
        } catch (Throwable ex) {
            OP_LOG.warn("Operation finished transactionId={} operation={} status=ERROR message={} durationMs={}",
                    txId, operation, ex.getMessage(), System.currentTimeMillis() - started);
            throw ex;
        }
    }
}
