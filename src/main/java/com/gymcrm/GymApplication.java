package com.gymcrm;

import com.gymcrm.config.AppConfig;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.storage.TraineeStorage;
import com.gymcrm.storage.TrainerStorage;
import com.gymcrm.storage.TrainingStorage;
import com.gymcrm.storage.TrainingTypeStorage;
import com.gymcrm.storage.UserStorage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class GymApplication {
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade facade = context.getBean(GymFacade.class);
        TrainingTypeStorage trainingTypeStorage = context.getBean(TrainingTypeStorage.class);

        // 1) Create Trainee profile → User + Trainee(userId FK)
        Trainee trainee = facade.createTrainee("Ani", "Kvatashidze", LocalDate.of(2005, 6, 9), "Gora");
        System.out.println("Created Trainee profile: id=" + trainee.getId()
                + ", userId=" + trainee.getUserId()
                + ", username=" + trainee.getUser().getUsername());

        // 2) Create Trainer profile → User + Trainer(userId FK, specialization)
        TrainingType cardio = trainingTypeStorage.requireByName("Cardio");
        Trainer trainer = facade.createTrainer("Giorgi", "Janelidze", cardio);
        System.out.println("Created Trainer profile: id=" + trainer.getId()
                + ", userId=" + trainer.getUserId()
                + ", username=" + trainer.getUser().getUsername()
                + ", specialization=" + trainer.getSpecialization().getTrainingTypeName());

        // 3) & 4) Username and password matching
        String traineeUsername = trainee.getUser().getUsername();
        String traineePassword = trainee.getUser().getPassword();
        String trainerUsername = trainer.getUser().getUsername();
        String trainerPassword = trainer.getUser().getPassword();

        System.out.println("\nTrainee credentials match (correct): "
                + facade.matchTraineeCredentials(traineeUsername, traineePassword));
        System.out.println("Trainee credentials match (wrong password): "
                + facade.matchTraineeCredentials(traineeUsername, "wrong-password"));
        System.out.println("Trainer credentials match (correct): "
                + facade.matchTrainerCredentials(trainerUsername, trainerPassword));
        System.out.println("Trainer credentials match (wrong password): "
                + facade.matchTrainerCredentials(trainerUsername, "wrong-password"));

        // 5) Select Trainer by username
        Trainer selectedTrainer = facade.selectTrainerByUsername(trainerUsername);
        System.out.println("\nSelected Trainer by username: id=" + selectedTrainer.getId()
                + ", userId=" + selectedTrainer.getUserId()
                + ", username=" + selectedTrainer.getUser().getUsername());

        // 6) Select Trainee by username
        Trainee selectedTrainee = facade.selectTraineeByUsername(traineeUsername);
        System.out.println("Selected Trainee by username: id=" + selectedTrainee.getId()
                + ", userId=" + selectedTrainee.getUserId()
                + ", username=" + selectedTrainee.getUser().getUsername());

        // 7) Trainee password change
        String newTraineePassword = "newTraineePass1";
        facade.changeTraineePassword(traineeUsername, traineePassword, newTraineePassword);
        System.out.println("\nTrainee password changed. Old match: "
                + facade.matchTraineeCredentials(traineeUsername, traineePassword)
                + ", New match: "
                + facade.matchTraineeCredentials(traineeUsername, newTraineePassword));

        // 8) Trainer password change
        String newTrainerPassword = "newTrainerPass1";
        facade.changeTrainerPassword(trainerUsername, trainerPassword, newTrainerPassword);
        System.out.println("Trainer password changed. Old match: "
                + facade.matchTrainerCredentials(trainerUsername, trainerPassword)
                + ", New match: "
                + facade.matchTrainerCredentials(trainerUsername, newTrainerPassword));

        // 9) Update Trainer profile (specialization from constant training types only)
        TrainingType yoga = trainingTypeStorage.requireByName("Yoga");
        Trainer updatedTrainer = facade.updateTrainer(
                trainer.getId(), "Giorgi", "Janelidze", yoga, true);
        System.out.println("\nUpdated Trainer: id=" + updatedTrainer.getId()
                + ", name=" + updatedTrainer.getUser().getFirstName() + " " + updatedTrainer.getUser().getLastName()
                + ", specialization=" + updatedTrainer.getSpecialization().getTrainingTypeName()
                + ", active=" + updatedTrainer.getUser().isActive());

        // 10) Update Trainee profile
        Trainee updatedTrainee = facade.updateTrainee(
                trainee.getId(), "Ani", "Kvatashidze",
                LocalDate.of(2005, 6, 9), "Vake", true);
        System.out.println("Updated Trainee: id=" + updatedTrainee.getId()
                + ", name=" + updatedTrainee.getUser().getFirstName() + " " + updatedTrainee.getUser().getLastName()
                + ", address=" + updatedTrainee.getAddress()
                + ", active=" + updatedTrainee.getUser().isActive());

        // 11) Activate / De-activate Trainee
        Trainee deactivatedTrainee = facade.setTraineeActive(trainee.getId(), false);
        System.out.println("\nTrainee de-activated: active=" + deactivatedTrainee.getUser().isActive());
        Trainee activatedTrainee = facade.setTraineeActive(trainee.getId(), true);
        System.out.println("Trainee activated: active=" + activatedTrainee.getUser().isActive());
        try {
            facade.setTraineeActive(trainee.getId(), true);
        } catch (IllegalStateException e) {
            System.out.println("Duplicate trainee activate rejected: " + e.getMessage());
        }

        // 12) Activate / De-activate Trainer
        Trainer deactivatedTrainer = facade.setTrainerActive(trainer.getId(), false);
        System.out.println("Trainer de-activated: active=" + deactivatedTrainer.getUser().isActive());
        Trainer activatedTrainer = facade.setTrainerActive(trainer.getId(), true);
        System.out.println("Trainer activated: active=" + activatedTrainer.getUser().isActive());
        try {
            facade.setTrainerActive(trainer.getId(), true);
        } catch (IllegalStateException e) {
            System.out.println("Duplicate trainer activate rejected: " + e.getMessage());
        }

        // 16) Add training
        LocalDate trainingDate = LocalDate.of(2024, 11, 20);
        Training addedTraining = facade.addTraining(
                traineeUsername,
                trainerUsername,
                "Morning Cardio",
                "Cardio",
                trainingDate,
                45);
        System.out.println("\nAdded training: id=" + addedTraining.getId()
                + ", name=" + addedTraining.getTrainingName()
                + ", date=" + addedTraining.getTrainingDate()
                + ", duration=" + addedTraining.getTrainingDuration());

        // 17) Get trainers not assigned to trainee
        TrainingType strength = trainingTypeStorage.requireByName("Strength");
        Trainer nikaTrainer = facade.createTrainer("Nika", "Beridze", strength);
        String nikaUsername = nikaTrainer.getUser().getUsername();
        List<Trainer> notAssignedTrainers = facade.getTrainersNotAssignedToTrainee(traineeUsername);
        System.out.println("\nTrainers not assigned to " + traineeUsername + ": " + notAssignedTrainers.size());
        notAssignedTrainers.forEach(t ->
                System.out.println("  -> " + t.getUser().getFirstName() + " " + t.getUser().getLastName()
                        + " (" + t.getUser().getUsername() + ")"));

        // 18) Update trainee's trainers list
        facade.updateTraineeTrainersList(traineeUsername, List.of(trainerUsername, nikaUsername));
        System.out.println("\nUpdated trainers list for " + traineeUsername + ": "
                + List.of(trainerUsername, nikaUsername));
        System.out.println("Trainers still not assigned: "
                + facade.getTrainersNotAssignedToTrainee(traineeUsername).size());

        // 14) Get Trainee Trainings List by username + criteria
        List<Training> traineeTrainings = facade.getTraineeTrainingsList(
                traineeUsername,
                LocalDate.of(2024, 11, 1),
                LocalDate.of(2024, 11, 30),
                "Giorgi Janelidze",
                "Cardio");
        System.out.println("\nTrainee trainings (filtered): " + traineeTrainings.size());
        traineeTrainings.forEach(t ->
                System.out.println("  -> " + t.getTrainingName()
                        + " | " + t.getTrainingDate()
                        + " | " + t.getTrainingType().getTrainingTypeName()));

        // 15) Get Trainer Trainings List by username + criteria
        List<Training> trainerTrainings = facade.getTrainerTrainingsList(
                trainerUsername,
                LocalDate.of(2024, 11, 1),
                LocalDate.of(2024, 11, 30),
                "Ani Kvatashidze");
        System.out.println("Trainer trainings (filtered): " + trainerTrainings.size());
        trainerTrainings.forEach(t ->
                System.out.println("  -> " + t.getTrainingName()
                        + " | " + t.getTrainingDate()
                        + " | traineeId=" + t.getTraineeId()));

        // 13) Hard-delete Trainee by username (cascade deletes trainings + User)
        int trainingsBeforeDelete = facade.selectAllTrainings().size();
        facade.deleteTraineeByUsername(traineeUsername);
        int trainingsAfterDelete = facade.selectAllTrainings().size();
        System.out.println("\nHard-deleted Trainee by username: " + traineeUsername);
        System.out.println("Trainings before delete: " + trainingsBeforeDelete
                + ", after cascade delete: " + trainingsAfterDelete);
        System.out.println("Trainees remaining: " + facade.selectAllTrainees().size());

        System.out.println("\nAll trainees: " + facade.selectAllTrainees());
        System.out.println("All trainers: " + facade.selectAllTrainers());

        System.out.println("\n**************** Storage dump ******************************");
        UserStorage userStorage = context.getBean(UserStorage.class);
        TraineeStorage traineeStorage = context.getBean(TraineeStorage.class);
        TrainerStorage trainerStorage = context.getBean(TrainerStorage.class);
        TrainingStorage trainingStorage = context.getBean(TrainingStorage.class);

        System.out.println("====== Users ======");
        userStorage.getStorage().forEach((id, u) ->
                System.out.println(id + " -> " + u.getUsername()));

        System.out.println("\n====== Trainees ======");
        traineeStorage.getStorage().forEach((id, t) ->
                System.out.println(id + " -> userId=" + t.getUserId() + ", address=" + t.getAddress()));

        System.out.println("\n====== Trainers ======");
        trainerStorage.getStorage().forEach((id, t) ->
                System.out.println(id + " -> userId=" + t.getUserId()
                        + ", specialization=" + t.getSpecialization().getTrainingTypeName()));

        System.out.println("\n====== Trainings ======");
        trainingStorage.getStorage().forEach((id, t) ->
                System.out.println(id + " -> " + t.getTrainingName()
                        + " (" + t.getTrainingType().getTrainingTypeName() + ")"));

        context.close();
    }
}
