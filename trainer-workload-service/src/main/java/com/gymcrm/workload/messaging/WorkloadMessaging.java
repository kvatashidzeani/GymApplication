package com.gymcrm.workload.messaging;

/**
 * ActiveMQ destinations and JMS headers shared with Gym CRM.
 */
public final class WorkloadMessaging {

    public static final String QUEUE = "workload.events.queue";
    public static final String DEAD_LETTER_QUEUE = "workload.events.dlq";

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String DLQ_REASON_HEADER = "X-DLQ-Reason";
    public static final String ORIGINAL_QUEUE_HEADER = "X-Original-Queue";

    private WorkloadMessaging() {
    }
}
