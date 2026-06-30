package com.gym.crm.Util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {
    private AtomicLong counter = new AtomicLong(1);

    public void initialize(Map<Long, ?> entityMap) {
        long maxId = entityMap.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        counter = new AtomicLong(maxId + 1);
    }

    public Long generateNextId() {
        return counter.getAndIncrement();
    }
}