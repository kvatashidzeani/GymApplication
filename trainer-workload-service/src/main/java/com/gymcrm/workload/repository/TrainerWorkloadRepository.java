package com.gymcrm.workload.repository;

import com.gymcrm.workload.model.TrainerWorkload;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for trainer training summaries.
 * <p>
 * Search and update operations are keyed by {@code trainerUsername} (document {@code _id}).
 */
public interface TrainerWorkloadRepository
        extends MongoRepository<TrainerWorkload, String>, TrainerWorkloadRepositoryCustom {

    /**
     * Search trainer summary by username.
     */
    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);

    /**
     * Search trainer summaries by first and last name (uses {@code trainer_name_idx} compound index).
     */
    List<TrainerWorkload> findByTrainerFirstNameAndTrainerLastName(String trainerFirstName, String trainerLastName);

    /**
     * Returns whether a summary exists for the given username.
     */
    boolean existsByTrainerUsername(String trainerUsername);

    /**
     * Deletes the trainer summary document for the given username.
     */
    void deleteByTrainerUsername(String trainerUsername);
}
