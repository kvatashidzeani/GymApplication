package com.gymcrm.actuator.health;

import com.gymcrm.Util.IdGenerator;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.User;
import com.gymcrm.storage.TraineeStorage;
import com.gymcrm.storage.TrainerStorage;
import com.gymcrm.storage.TrainingStorage;
import com.gymcrm.storage.TrainingTypeStorage;
import com.gymcrm.storage.UserStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthIndicatorsTest {

    @Test
    void inMemoryStorage_upWhenSeeded() {
        TraineeStorage trainees = new TraineeStorage();
        TrainerStorage trainers = new TrainerStorage();
        TrainingStorage trainings = new TrainingStorage();
        UserStorage users = new UserStorage();

        User user = new User("A", "B", "A.B", "pass", true, 1L);
        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainees.getStorage().put(1L, trainee);
        users.getStorage().put(1L, user);

        Health health = new InMemoryStorageHealthIndicator(trainees, trainers, trainings, users).health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(1, health.getDetails().get("trainees"));
    }

    @Test
    void inMemoryStorage_downWhenEmpty() {
        Health health = new InMemoryStorageHealthIndicator(
                new TraineeStorage(), new TrainerStorage(), new TrainingStorage(), new UserStorage())
                .health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void trainingTypeCatalog_upWhenPresent() {
        TrainingTypeStorage storage = new TrainingTypeStorage();
        storage.setIdGenerator(new IdGenerator());
        storage.seedTrainingType("Cardio");

        Health health = new TrainingTypeCatalogHealthIndicator(storage).health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(1, health.getDetails().get("trainingTypes"));
    }

    @Test
    void trainingTypeCatalog_downWhenEmpty() {
        Health health = new TrainingTypeCatalogHealthIndicator(new TrainingTypeStorage()).health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
