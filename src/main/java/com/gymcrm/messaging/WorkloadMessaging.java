package com.gymcrm.messaging;

/**
 * ActiveMQ destination for trainer workload events (training ADD/DELETE).
 */
public final class WorkloadMessaging {

    public static final String QUEUE = "workload.events.queue";
    public static final String DEAD_LETTER_QUEUE = "workload.events.dlq";

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private WorkloadMessaging() {
    }
}
