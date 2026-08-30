package com.gymcrm.workload.repository;

import com.gymcrm.workload.model.TrainerWorkload;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * MongoTemplate-backed update implementation for {@link TrainerWorkloadRepository}.
 */
@Repository
public class TrainerWorkloadRepositoryImpl implements TrainerWorkloadRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public TrainerWorkloadRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public TrainerWorkload updateByTrainerUsername(TrainerWorkload workload) {
        if (workload == null) {
            throw new IllegalArgumentException("workload is required");
        }
        String username = workload.getTrainerUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("trainerUsername is required for update");
        }

        Query query = Query.query(Criteria.where("_id").is(username.trim()));
        TrainerWorkload replaced = mongoTemplate.findAndReplace(
                query,
                workload,
                FindAndReplaceOptions.options().upsert().returnNew());

        return replaced != null ? replaced : mongoTemplate.save(workload);
    }
}
