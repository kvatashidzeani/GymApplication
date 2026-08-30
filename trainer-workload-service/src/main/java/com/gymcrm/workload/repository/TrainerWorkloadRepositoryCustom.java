package com.gymcrm.workload.repository;

import com.gymcrm.workload.model.TrainerWorkload;

/**
 * Custom MongoDB operations for trainer workload documents.
 */
public interface TrainerWorkloadRepositoryCustom {

    /**
     * Replaces the trainer document matched by username (upsert if missing).
     */
    TrainerWorkload updateByTrainerUsername(TrainerWorkload workload);
}
