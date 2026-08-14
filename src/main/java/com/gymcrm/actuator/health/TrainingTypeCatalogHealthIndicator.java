package com.gymcrm.actuator.health;

import com.gymcrm.storage.TrainingTypeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Training types are a constant catalog seeded at startup; empty catalog means the app cannot create trainings.
 */
@Component("trainingTypeCatalog")
public class TrainingTypeCatalogHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeCatalogHealthIndicator.class);

    private final TrainingTypeStorage trainingTypeStorage;

    public TrainingTypeCatalogHealthIndicator(TrainingTypeStorage trainingTypeStorage) {
        this.trainingTypeStorage = trainingTypeStorage;
    }

    @Override
    public Health health() {
        try {
            int count = trainingTypeStorage.findAll().size();
            if (count == 0) {
                log.warn("Training type catalog health DOWN: no training types loaded");
                return Health.down()
                        .withDetail("reason", "Training type catalog is empty")
                        .withDetail("trainingTypes", 0)
                        .build();
            }
            log.debug("Training type catalog health UP: {} types", count);
            return Health.up()
                    .withDetail("trainingTypes", count)
                    .withDetail("names", trainingTypeStorage.findAll().stream()
                            .map(t -> t.getTrainingTypeName())
                            .toList())
                    .build();
        } catch (Exception ex) {
            log.error("Training type catalog health check failed", ex);
            return Health.down(ex).withDetail("reason", "Failed to read training type catalog").build();
        }
    }
}
