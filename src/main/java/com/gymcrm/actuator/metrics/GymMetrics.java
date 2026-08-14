package com.gymcrm.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Custom Micrometer metrics exported via the Prometheus scrape endpoint ({@code /actuator/prometheus}).
 */
@Component
public class GymMetrics {

    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter trainingsCreatedCounter;
    private final Counter traineeRegistrationsCounter;
    private final Counter trainerRegistrationsCounter;
    private final Timer loginTimer;
    private final Timer trainingCreateTimer;

    public GymMetrics(MeterRegistry registry) {
        this.loginSuccessCounter = Counter.builder("gymcrm.login.attempts")
                .description("Number of login attempts")
                .tag("result", "success")
                .register(registry);
        this.loginFailureCounter = Counter.builder("gymcrm.login.attempts")
                .description("Number of login attempts")
                .tag("result", "failure")
                .register(registry);
        this.trainingsCreatedCounter = Counter.builder("gymcrm.trainings.created")
                .description("Number of trainings created via REST")
                .register(registry);
        this.traineeRegistrationsCounter = Counter.builder("gymcrm.registrations")
                .description("Number of user registrations")
                .tag("role", "trainee")
                .register(registry);
        this.trainerRegistrationsCounter = Counter.builder("gymcrm.registrations")
                .description("Number of user registrations")
                .tag("role", "trainer")
                .register(registry);
        this.loginTimer = Timer.builder("gymcrm.login.duration")
                .description("Login request duration")
                .register(registry);
        this.trainingCreateTimer = Timer.builder("gymcrm.trainings.create.duration")
                .description("Add-training request duration")
                .register(registry);
    }

    public void loginSucceeded() {
        loginSuccessCounter.increment();
    }

    public void loginFailed() {
        loginFailureCounter.increment();
    }

    public void trainingCreated() {
        trainingsCreatedCounter.increment();
    }

    public void traineeRegistered() {
        traineeRegistrationsCounter.increment();
    }

    public void trainerRegistered() {
        trainerRegistrationsCounter.increment();
    }

    public Timer.Sample startLoginTimer() {
        return Timer.start();
    }

    public void stopLoginTimer(Timer.Sample sample) {
        sample.stop(loginTimer);
    }

    public Timer.Sample startTrainingCreateTimer() {
        return Timer.start();
    }

    public void stopTrainingCreateTimer(Timer.Sample sample) {
        sample.stop(trainingCreateTimer);
    }
}
