package com.gymcrm.storage;

import com.gymcrm.config.AppConfig;
import com.gymcrm.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class StorageInitializerIntegrationTest {

    @Test
    void storageIsPopulatedOnStartup() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        UserStorage userStorage = context.getBean(UserStorage.class);
        TraineeStorage traineeStorage = context.getBean(TraineeStorage.class);
        TrainerStorage trainerStorage = context.getBean(TrainerStorage.class);
        TrainingStorage trainingStorage = context.getBean(TrainingStorage.class);

        assertFalse(userStorage.getStorage().isEmpty(), "Users should be loaded");
        assertFalse(traineeStorage.getStorage().isEmpty(), "Trainees should be loaded");
        assertFalse(trainerStorage.getStorage().isEmpty(), "Trainers should be loaded");
        assertFalse(trainingStorage.getStorage().isEmpty(), "Trainings should be loaded");

        context.close();
    }

    @Test
    void loadedTraineesHaveValidUsernamesViaUser() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        UserStorage userStorage = context.getBean(UserStorage.class);
        TraineeStorage traineeStorage = context.getBean(TraineeStorage.class);

        traineeStorage.getStorage().values().forEach(t -> {
            assertNotNull(t.getUserId(), "Trainee must have userId FK");
            User user = userStorage.getStorage().get(t.getUserId());
            assertNotNull(user, "Linked User must exist");
            assertNotNull(user.getUsername());
            assertFalse(user.getUsername().isEmpty());
            assertTrue(user.getUsername().contains("."));
        });

        context.close();
    }
}
