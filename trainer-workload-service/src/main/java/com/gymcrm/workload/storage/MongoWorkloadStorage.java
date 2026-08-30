package com.gymcrm.workload.storage;

import com.gymcrm.workload.model.TrainerWorkload;
import com.gymcrm.workload.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * MongoDB-backed workload storage using {@link TrainerWorkloadRepository}.
 */
@Component
public class MongoWorkloadStorage implements WorkloadStorage {

    private final TrainerWorkloadRepository repository;

    public MongoWorkloadStorage(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<TrainerWorkload> findByUsername(String trainerUsername) {
        return repository.findByTrainerUsername(trainerUsername);
    }

    @Override
    public TrainerWorkload save(TrainerWorkload workload) {
        if (repository.existsByTrainerUsername(workload.getTrainerUsername())) {
            return repository.updateByTrainerUsername(workload);
        }
        return repository.save(workload);
    }

    @Override
    public void delete(String trainerUsername) {
        repository.deleteByTrainerUsername(trainerUsername);
    }
}
