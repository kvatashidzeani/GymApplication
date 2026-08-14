package com.gymcrm.actuator.health;

import com.gymcrm.storage.TraineeStorage;
import com.gymcrm.storage.TrainerStorage;
import com.gymcrm.storage.TrainingStorage;
import com.gymcrm.storage.UserStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Reports health of the in-memory HashMap storages used by the CRM DAOs.
 */
@Component("inMemoryStorage")
public class InMemoryStorageHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(InMemoryStorageHealthIndicator.class);

    private final TraineeStorage traineeStorage;
    private final TrainerStorage trainerStorage;
    private final TrainingStorage trainingStorage;
    private final UserStorage userStorage;

    public InMemoryStorageHealthIndicator(
            TraineeStorage traineeStorage,
            TrainerStorage trainerStorage,
            TrainingStorage trainingStorage,
            UserStorage userStorage) {
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
        this.trainingStorage = trainingStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Health health() {
        try {
            int trainees = traineeStorage.getStorage().size();
            int trainers = trainerStorage.getStorage().size();
            int trainings = trainingStorage.getStorage().size();
            int users = userStorage.getStorage().size();

            if (trainees == 0 && trainers == 0 && users == 0) {
                log.warn("In-memory storage health DOWN: empty storages (trainees={}, trainers={}, trainings={}, users={})",
                        trainees, trainers, trainings, users);
                return Health.down()
                        .withDetail("reason", "In-memory storages are empty (seed data missing?)")
                        .withDetail("trainees", trainees)
                        .withDetail("trainers", trainers)
                        .withDetail("trainings", trainings)
                        .withDetail("users", users)
                        .build();
            }

            log.debug("In-memory storage health UP: trainees={}, trainers={}, trainings={}, users={}",
                    trainees, trainers, trainings, users);
            return Health.up()
                    .withDetail("trainees", trainees)
                    .withDetail("trainers", trainers)
                    .withDetail("trainings", trainings)
                    .withDetail("users", users)
                    .build();
        } catch (Exception ex) {
            log.error("In-memory storage health check failed", ex);
            return Health.down(ex).withDetail("reason", "Failed to read in-memory storages").build();
        }
    }
}
