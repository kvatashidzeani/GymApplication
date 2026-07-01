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

    public Trainee selectTrainee(Long id) {
        return traineeService.select(id);
    }

    public List<Trainee> selectAllTrainees() {
        return traineeService.selectAllTrainees();
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

    public List<Trainer> selectAllTrainers() {
        return trainerService.selectAllTrainers();
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