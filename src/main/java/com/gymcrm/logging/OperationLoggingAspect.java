package com.gymcrm.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Operation-level logging for service / facade methods.
 * All messages share the current {@code transactionId} via MDC.
 */
@Aspect
@Component
public class OperationLoggingAspect {

    private static final Logger OP_LOG = LoggerFactory.getLogger("com.gymcrm.logging.Operation");

    private static final int MAX_ARG_CHARS = 500;

    @Around("execution(* com.gymcrm.service..*(..)) || execution(* com.gymcrm.facade..*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String operation = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String args = summarizeArgs(joinPoint.getArgs());
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

    private static String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "()";
        }
        String joined = Arrays.stream(args)
                .map(OperationLoggingAspect::safeArg)
                .collect(Collectors.joining(", "));
        if (joined.length() > MAX_ARG_CHARS) {
            return "(" + joined.substring(0, MAX_ARG_CHARS) + "...(truncated))";
        }
        return "(" + joined + ")";
    }

    private static String safeArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        String text = String.valueOf(arg);
        String lower = text.toLowerCase();
        if (lower.contains("password") || arg.getClass().getSimpleName().toLowerCase().contains("password")) {
            return "***";
        }
        return SensitiveDataMasker.maskBody(text);
    }
}
