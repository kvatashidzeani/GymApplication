package com.gymcrm.facade;

import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.service.TraineeService;
import com.gymcrm.service.TrainerService;
import com.gymcrm.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {

    private static final Logger logger = LoggerFactory.getLogger(GymFacade.class);

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // ── Trainee operations ──────────────────────────────────────────────────

    public Trainee createTrainee(Trainee trainee) {
        logger.info("Facade: creating trainee {} {}", trainee.getFirstName(), trainee.getLastName());
        return traineeService.createTrainee(trainee);
    }

    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Facade: updating trainee userId={}", trainee.getUserId());
        return traineeService.updateTrainee(trainee);
    }

    public void deleteTrainee(Long id) {
        logger.info("Facade: deleting trainee id={}", id);
        traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> selectTrainee(Long id) {
        logger.debug("Facade: selecting trainee id={}", id);
        return traineeService.selectTrainee(id);
    }

    public List<Trainee> selectAllTrainees() {
        logger.debug("Facade: selecting all trainees");
        return traineeService.selectAllTrainees();
    }

    // ── Trainer operations ──────────────────────────────────────────────────

    public Trainer createTrainer(Trainer trainer) {
        logger.info("Facade: creating trainer {} {}", trainer.getFirstName(), trainer.getLastName());
        return trainerService.createTrainer(trainer);
    }

    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Facade: updating trainer userId={}", trainer.getUserId());
        return trainerService.updateTrainer(trainer);
    }

    public Optional<Trainer> selectTrainer(Long id) {
        logger.debug("Facade: selecting trainer id={}", id);
        return trainerService.selectTrainer(id);
    }

    public List<Trainer> selectAllTrainers() {
        logger.debug("Facade: selecting all trainers");
        return trainerService.selectAllTrainers();
    }

    // ── Training operations ─────────────────────────────────────────────────

    public Training createTraining(Training training) {
        logger.info("Facade: creating training {}", training.getTrainingName());
        return trainingService.createTraining(training);
    }

    public Optional<Training> selectTraining(Long id) {
        logger.debug("Facade: selecting training id={}", id);
        return trainingService.selectTraining(id);
    }

    public List<Training> selectAllTrainings() {
        logger.debug("Facade: selecting all trainings");
        return trainingService.selectAllTrainings();
    }
}