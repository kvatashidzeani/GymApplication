package org.example.Storage;


import com.gym.crm.config.AppConfig;
import com.gym.crm.storage.TraineeStorage;
import com.gym.crm.storage.TrainerStorage;
import com.gym.crm.storage.TrainingStorage;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class StorageInitializerIntegrationTest {

    @Test
    void storageIsPopulatedOnStartup() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        TraineeStorage traineeStorage = context.getBean(TraineeStorage.class);
        TrainerStorage trainerStorage = context.getBean(TrainerStorage.class);
        TrainingStorage trainingStorage = context.getBean(TrainingStorage.class);

        assertFalse(traineeStorage.getStorage().isEmpty(), "Trainees should be loaded");
        assertFalse(trainerStorage.getStorage().isEmpty(), "Trainers should be loaded");
        assertFalse(trainingStorage.getStorage().isEmpty(), "Trainings should be loaded");

        context.close();
    }

    @Test
    void loadedTraineesHaveValidUsernames() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        TraineeStorage traineeStorage = context.getBean(TraineeStorage.class);

        traineeStorage.getStorage().values().forEach(t -> {
            assertNotNull(t.getUsername());
            assertFalse(t.getUsername().isEmpty());
            assertTrue(t.getUsername().contains("."));
        });

        context.close();
    }
}