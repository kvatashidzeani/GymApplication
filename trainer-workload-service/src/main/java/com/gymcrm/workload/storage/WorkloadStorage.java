package com.gymcrm.workload.storage;

import com.gymcrm.workload.model.TrainerWorkload;

import java.util.Optional;

/**
 * Persistence port for trainer training summaries (MongoDB in production).
 */
public interface WorkloadStorage {

    Optional<TrainerWorkload> findByUsername(String trainerUsername);

    TrainerWorkload save(TrainerWorkload workload);

    void delete(String trainerUsername);
}
