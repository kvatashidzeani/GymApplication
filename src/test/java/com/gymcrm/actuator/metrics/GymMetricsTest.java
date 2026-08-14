package com.gymcrm.actuator.metrics;

import com.gymcrm.storage.TraineeStorage;
import com.gymcrm.storage.TrainerStorage;
import com.gymcrm.storage.TrainingStorage;
import com.gymcrm.storage.UserStorage;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GymMetricsTest {

    @Test
    void countersIncrement() {
        MeterRegistry registry = new SimpleMeterRegistry();
        GymMetrics metrics = new GymMetrics(registry);

        metrics.loginSucceeded();
        metrics.loginFailed();
        metrics.trainingCreated();
        metrics.traineeRegistered();
        metrics.trainerRegistered();

        assertEquals(1.0, registry.get("gymcrm.login.attempts").tag("result", "success").counter().count());
        assertEquals(1.0, registry.get("gymcrm.login.attempts").tag("result", "failure").counter().count());
        assertEquals(1.0, registry.get("gymcrm.trainings.created").counter().count());
        assertEquals(1.0, registry.get("gymcrm.registrations").tag("role", "trainee").counter().count());
        assertEquals(1.0, registry.get("gymcrm.registrations").tag("role", "trainer").counter().count());
    }

    @Test
    void storageGaugesReflectMapSizes() {
        MeterRegistry registry = new SimpleMeterRegistry();
        TraineeStorage trainees = new TraineeStorage();
        TrainerStorage trainers = new TrainerStorage();
        TrainingStorage trainings = new TrainingStorage();
        UserStorage users = new UserStorage();

        GymStorageMetrics gauges = new GymStorageMetrics(registry, trainees, trainers, trainings, users);
        gauges.registerGauges();

        trainees.getStorage().put(1L, new com.gymcrm.model.Trainee());
        trainers.getStorage().put(1L, new com.gymcrm.model.Trainer());
        trainings.getStorage().put(1L, new com.gymcrm.model.Training());
        users.getStorage().put(1L, new com.gymcrm.model.User("A", "B", "A.B", "p", true, 1L));

        assertEquals(1.0, registry.get("gymcrm.storage.trainees").gauge().value());
        assertEquals(1.0, registry.get("gymcrm.storage.trainers").gauge().value());
        assertEquals(1.0, registry.get("gymcrm.storage.trainings").gauge().value());
        assertEquals(1.0, registry.get("gymcrm.storage.users").gauge().value());
    }
}
