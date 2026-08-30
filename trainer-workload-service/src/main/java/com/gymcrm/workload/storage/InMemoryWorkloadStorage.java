package com.gymcrm.workload.storage;

import com.gymcrm.workload.model.TrainerWorkload;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory implementation for unit tests (no MongoDB required).
 */
public class InMemoryWorkloadStorage implements WorkloadStorage {

    private final ConcurrentMap<String, TrainerWorkload> byUsername = new ConcurrentHashMap<>();

    @Override
    public Optional<TrainerWorkload> findByUsername(String trainerUsername) {
        return Optional.ofNullable(byUsername.get(trainerUsername));
    }

    @Override
    public TrainerWorkload save(TrainerWorkload workload) {
        byUsername.put(workload.getTrainerUsername(), workload);
        return workload;
    }

    @Override
    public void delete(String trainerUsername) {
        byUsername.remove(trainerUsername);
    }

    public void clear() {
        byUsername.clear();
    }
}
