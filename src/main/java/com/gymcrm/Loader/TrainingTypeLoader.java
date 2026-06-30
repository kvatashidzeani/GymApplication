package com.gym.crm.Loader;

import com.gym.crm.model.TrainingType;
import com.gym.crm.storage.TrainingTypeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeLoader implements Loader {

    private TrainingTypeStorage trainingTypeStorage;
    private SeedDataContext context;
    private final Logger log = LoggerFactory.getLogger(TrainingTypeLoader.class);

    @Autowired
    public void setTrainingTypeStorage(TrainingTypeStorage trainingTypeStorage) {
        this.trainingTypeStorage = trainingTypeStorage;
    }

    @Autowired
    public void setContext(SeedDataContext context) {
        this.context = context;
    }

    @Override
    public int getOrder() { return 1; }

    @Override
    public void load() {
        var trainingTypes = context.getSeedData().getTrainingTypes();
        if (trainingTypes == null || trainingTypes.isEmpty()) {
            log.warn("No training types to load");
            return;
        }

        trainingTypes.forEach(name -> {
            TrainingType type = trainingTypeStorage.addTrainingType(name);
            log.info("Seeded TrainingType: {} id={}", type.getName(), type.getId());
        });
        log.info("Successfully parsed all of the Training Types.");
    }
}