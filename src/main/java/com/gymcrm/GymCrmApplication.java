package com.gymcrm;

import com.gymcrm.config.AppConfig;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class GymCrmApplication {

    private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Gym CRM Application...");

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade facade = context.getBean(GymFacade.class);

            logger.info("=== Trainers loaded from storage ===");
            facade.selectAllTrainers().forEach(t -> logger.info("  {}", t));

            logger.info("=== Trainees loaded from storage ===");
            facade.selectAllTrainees().forEach(t -> logger.info("  {}", t));

            logger.info("=== Trainings loaded from storage ===");
            facade.selectAllTrainings().forEach(t -> logger.info("  {}", t));

            // Create a new trainer
            Trainer newTrainer = new Trainer();
            newTrainer.setFirstName("John");
            newTrainer.setLastName("Smith");
            newTrainer.setUserId(100L);
            newTrainer.setSpecialization("Strength Training");
            Trainer created = facade.createTrainer(newTrainer);
            logger.info("Created trainer: username={}", created.getUsername());

            // Create duplicate name trainer to test suffix logic
            Trainer dupTrainer = new Trainer();
            dupTrainer.setFirstName("John");
            dupTrainer.setLastName("Smith");
            dupTrainer.setUserId(101L);
            dupTrainer.setSpecialization("Cardio");
            Trainer dup = facade.createTrainer(dupTrainer);
            logger.info("Created duplicate trainer: username={}", dup.getUsername());

            // Create a trainee
            Trainee trainee = new Trainee();
            trainee.setFirstName("Alice");
            trainee.setLastName("Johnson");
            trainee.setUserId(200L);
            trainee.setDateOfBirth(LocalDate.of(1995, 6, 15));
            trainee.setAddress("123 Main St");
            Trainee createdTrainee = facade.createTrainee(trainee);
            logger.info("Created trainee: username={}", createdTrainee.getUsername());

            // Create a training
            Training training = new Training(
                    999L, 200L, 100L, "Morning Session",
                    new TrainingType("Strength Training"),
                    LocalDate.now(), 60
            );
            Training createdTraining = facade.createTraining(training);
            logger.info("Created training: {}", createdTraining.getTrainingName());

        }

        logger.info("Gym CRM Application finished.");
    }
}