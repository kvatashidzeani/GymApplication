package com.gymcrm.facade;


import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.TraineeService;
import com.gymcrm.service.TrainerService;
import com.gymcrm.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService
    ) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(String firstName,
                                 String lastName,
                                 LocalDate dateOfBirth,
                                 String address) {
        return traineeService.createTrainee(firstName, lastName, dateOfBirth, address);
    }

    public Trainee updateTrainee(Long id,
                                 String firstName,
                                 String lastName,
                                 LocalDate dateOfBirth,
                                 String address,
                                 Boolean isActive) {
        return traineeService.updateTrainee(id, firstName, lastName, dateOfBirth, address, isActive);
    }

    public void deleteTrainee(Long id) {
        traineeService.deleteTrainee(id);
    }

    public void deleteTraineeByUsername(String username) {
        traineeService.deleteTraineeByUsername(username);
    }

    public List<Training> getTraineeTrainingsList(String username,
                                                  LocalDate fromDate,
                                                  LocalDate toDate,
                                                  String trainerName,
                                                  String trainingType) {
        return traineeService.getTraineeTrainingsList(
                username, fromDate, toDate, trainerName, trainingType);
    }

    public Trainee selectTrainee(Long id) {
        return traineeService.select(id);
    }

    public Trainee selectTraineeByUsername(String username) {
        return traineeService.selectTraineeByUsername(username);
    }

    public List<Trainee> selectAllTrainees() {
        return traineeService.selectAllTrainees();
    }

    public boolean matchTraineeCredentials(String username, String password) {
        return traineeService.matchTraineeCredentials(username, password);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changeTraineePassword(username, oldPassword, newPassword);
    }

    public Trainee setTraineeActive(Long id, boolean isActive) {
        return traineeService.setTraineeActive(id, isActive);
    }

    public Trainer createTrainer(String firstName,
                                 String lastName,
                                 TrainingType specialization) {
        return trainerService.createTrainer(firstName, lastName, specialization);
    }

    public Trainer updateTrainer(Long id,
                                 String firstName,
                                 String lastName,
                                 TrainingType specialization,
                                 Boolean isActive) {
        return trainerService.updateTrainer(id, firstName, lastName, specialization, isActive);
    }

    public Trainer selectTrainer(Long id) {
        return trainerService.selectTrainer(id);
    }

    public Trainer selectTrainerByUsername(String username) {
        return trainerService.selectTrainerByUsername(username);
    }

    public List<Trainer> selectAllTrainers() {
        return trainerService.selectAllTrainers();
    }

    public boolean matchTrainerCredentials(String username, String password) {
        return trainerService.matchTrainerCredentials(username, password);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changeTrainerPassword(username, oldPassword, newPassword);
    }

    public Trainer setTrainerActive(Long id, boolean isActive) {
        return trainerService.setTrainerActive(id, isActive);
    }

    public List<Training> getTrainerTrainingsList(String username,
                                                  LocalDate fromDate,
                                                  LocalDate toDate,
                                                  String traineeName) {
        return trainerService.getTrainerTrainingsList(username, fromDate, toDate, traineeName);
    }

    public Training createTraining(Long traineeId,
                                   Long trainerId,
                                   String name,
                                   TrainingType type,
                                   LocalDate date,
                                   Integer durationMinutes) {
        return trainingService.createTraining(
                traineeId,
                trainerId,
                name,
                type,
                date,
                durationMinutes
        );
    }

    public Training selectTraining(Long id) {
        return trainingService.selectTraining(id);
    }

    public List<Training> selectAllTrainings() {
        return trainingService.selectAllTrainings();
    }
}
