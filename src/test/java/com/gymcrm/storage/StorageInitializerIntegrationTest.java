package com.gymcrm.storage;

import com.gymcrm.config.InMemoryTestConfig;
import com.gymcrm.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StorageInitializerIntegrationTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource("integration-test", Map.of(
                        "data.storage", "classpath:initial-data.json"
                ))
        );
        context.register(InMemoryTestConfig.class);
        context.refresh();
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void storageIsPopulatedOnStartup() {
        TraineeStorage traineeStorage = context.getBean(TraineeStorage.class);
        TrainerStorage trainerStorage = context.getBean(TrainerStorage.class);
        TrainingStorage trainingStorage = context.getBean(TrainingStorage.class);
        UserStorage userStorage = context.getBean(UserStorage.class);

        assertFalse(traineeStorage.getStorage().isEmpty(), "Trainees should be loaded");
        assertFalse(trainerStorage.getStorage().isEmpty(), "Trainers should be loaded");
        assertFalse(trainingStorage.getStorage().isEmpty(), "Trainings should be loaded");
        assertFalse(userStorage.getStorage().isEmpty(), "Users should be loaded");
    }

    @Test
    void loadedTraineesHaveValidUsernames() {
        TraineeStorage traineeStorage = context.getBean(TraineeStorage.class);
        UserStorage userStorage = context.getBean(UserStorage.class);

        assertFalse(traineeStorage.getStorage().isEmpty(), "Trainees should be loaded");

        traineeStorage.getStorage().values().forEach(trainee -> {
            User user = trainee.getUser();
            if (user == null) {
                user = userStorage.getStorage().get(trainee.getUserId());
            }
            assertNotNull(user, "User should exist for trainee id=" + trainee.getId());
            assertNotNull(user.getUsername());
            assertFalse(user.getUsername().isEmpty());
            assertTrue(user.getUsername().contains("."), "Username should be First.Last format");
        });
    }
}
