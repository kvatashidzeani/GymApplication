package com.gymcrm.Loader;

import com.gymcrm.service.TraineeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TraineeLoader implements Loader {

    private TraineeService traineeService;
    private SeedDataContext context;
    private final Logger log = LoggerFactory.getLogger(TraineeLoader.class);

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }
    @Autowired
    public void setContext(SeedDataContext context) {
        this.context = context;
    }

    @Override
    public int getOrder() { return 2; }

    @Override
    public void load() {
        var trainees = context.getSeedData().getTrainees();
        if (trainees == null || trainees.isEmpty()) {
            log.warn("No trainees to load.");
            return;
        }

        trainees.forEach(t -> {
            var created = traineeService.createTrainee(
                    t.getFirstName(), t.getLastName(), t.getDateOfBirth(), t.getAddress()
            );
            if (created.getUser() != null) {
                created.getUser().setRawPassword(null);
            }
            log.info("Seeded Trainee: {} {}", t.getFirstName(), t.getLastName());
        });
        log.info("Successfully parsed all of the Trainees.");
    }
}
