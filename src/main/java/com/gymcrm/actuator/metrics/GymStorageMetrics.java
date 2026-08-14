package com.gymcrm.actuator.metrics;

import com.gymcrm.storage.TraineeStorage;
import com.gymcrm.storage.TrainerStorage;
import com.gymcrm.storage.TrainingStorage;
import com.gymcrm.storage.UserStorage;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Gauges that expose current in-memory entity counts to Prometheus.
 */
@Component
public class GymStorageMetrics {

    private final MeterRegistry registry;
    private final TraineeStorage traineeStorage;
    private final TrainerStorage trainerStorage;
    private final TrainingStorage trainingStorage;
    private final UserStorage userStorage;

    public GymStorageMetrics(
            MeterRegistry registry,
            TraineeStorage traineeStorage,
            TrainerStorage trainerStorage,
            TrainingStorage trainingStorage,
            UserStorage userStorage) {
        this.registry = registry;
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
        this.trainingStorage = trainingStorage;
        this.userStorage = userStorage;
    }

    @PostConstruct
    void registerGauges() {
        Gauge.builder("gymcrm.storage.trainees", traineeStorage, s -> s.getStorage().size())
                .description("Current number of trainees in memory")
                .register(registry);
        Gauge.builder("gymcrm.storage.trainers", trainerStorage, s -> s.getStorage().size())
                .description("Current number of trainers in memory")
                .register(registry);
        Gauge.builder("gymcrm.storage.trainings", trainingStorage, s -> s.getStorage().size())
                .description("Current number of trainings in memory")
                .register(registry);
        Gauge.builder("gymcrm.storage.users", userStorage, s -> s.getStorage().size())
                .description("Current number of users in memory")
                .register(registry);
        Gauge.builder("gymcrm.storage.active.trainees", traineeStorage,
                        s -> s.getStorage().values().stream()
                                .filter(t -> t.getUser() != null && t.getUser().isActive())
                                .count())
                .description("Number of active trainees")
                .register(registry);
        Gauge.builder("gymcrm.storage.active.trainers", trainerStorage,
                        s -> s.getStorage().values().stream()
                                .filter(t -> t.getUser() != null && t.getUser().isActive())
                                .count())
                .description("Number of active trainers")
                .register(registry);
    }
}
