package com.gym.crm.Loader;

import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainerService;
import com.gym.crm.storage.TrainingTypeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TrainerLoader implements Loader {

    private TrainerService trainerService;
    private SeedDataContext context;
    private TrainingTypeStorage trainingTypeStorage;
    private final Logger log = LoggerFactory.getLogger(TrainerLoader.class);

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setContext(SeedDataContext context) {
        this.context = context;
    }

    @Autowired
    public void setTrainingTypeStorage(TrainingTypeStorage trainingTypeStorage) {
        this.trainingTypeStorage = trainingTypeStorage;
    }

    @Override
    public int getOrder() { return 3; }

    @Override
    public void load() {
        var trainers = context.getSeedData().getTrainers();
        if (trainers == null || trainers.isEmpty()) {
            log.warn("No trainers to load.");
            return;
        }

        Map<String, TrainingType> typeMap = trainingTypeStorage.getAllByName();

        trainers.forEach(t -> {
            TrainingType specialization = typeMap.get(t.getSpecializationName());
            if (specialization == null) {
                log.error("Unknown specialization: {}", t.getSpecializationName());
            }

            trainerService.createTrainer(t.getFirstName(), t.getLastName(), specialization);
            log.info("Seeded Trainer: {} {} ({})", t.getFirstName(), t.getLastName(), specialization.getName());
        });
    }
}