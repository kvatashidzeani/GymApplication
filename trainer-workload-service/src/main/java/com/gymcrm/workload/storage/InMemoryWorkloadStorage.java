package com.gymcrm.workload.storage;

import com.gymcrm.workload.model.TrainerWorkload;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory DB: trainerUsername → nested TrainerWorkload (Years → Months → duration).
 */
@Component
public class InMemoryWorkloadStorage {

    private final ConcurrentMap<String, TrainerWorkload> byUsername = new ConcurrentHashMap<>();

    public Optional<TrainerWorkload> findByUsername(String trainerUsername) {
        return Optional.ofNullable(byUsername.get(trainerUsername));
    }

    public TrainerWorkload save(TrainerWorkload workload) {
        byUsername.put(workload.getTrainerUsername(), workload);
        return workload;
    }

    public void delete(String trainerUsername) {
        byUsername.remove(trainerUsername);
    }

    public Collection<TrainerWorkload> findAll() {
        return byUsername.values();
    }

    public void clear() {
        byUsername.clear();
    }
}
