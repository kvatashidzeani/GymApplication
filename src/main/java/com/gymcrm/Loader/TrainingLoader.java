package com.gym.crm.Loader;

import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainingService;
import com.gym.crm.storage.TrainingTypeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrainingLoader implements Loader {

    private TrainingService trainingService;
    private SeedDataContext context;
    private TrainingTypeStorage trainingTypeStorage;
    private final Logger log = LoggerFactory.getLogger(TrainingLoader.class);

    @Autowired
    public void setTrainingService(TrainingService trainingService) {
        this.trainingService = trainingService;
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
    public int getOrder() {
        return 3;
    }

    public void load() {
        var trainings = context.getSeedData().getTrainings();
        if (trainings == null) return;

        var typeMap = trainingTypeStorage.getAllByName();

        trainings.forEach(t -> {
            TrainingType type = typeMap.get(t.getTrainingTypeName());
            if (type == null) {
                log.error("The given type does not exist.");
                return;
            }

            trainingService.createTraining(
                    t.getTraineeId(),
                    t.getTrainerId(),
                    t.getTrainingName(),
                    type,
                    t.getTrainingDate(),
                    t.getTrainingDurationMinutes()
            );
            log.info("Seeded training: {}",t.getTrainingName());
        });
        log.info("Successfully parsed the trainings.");
    }
}