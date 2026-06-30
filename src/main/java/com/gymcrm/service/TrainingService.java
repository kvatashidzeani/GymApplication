package com.gym.crm.service;

import com.gym.crm.exceptions.TrainingNotFoundException;
import com.gym.crm.Util.IdGenerator;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.validators.TrainingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);


    private TrainingDao trainingDao;
    private IdGenerator idGenerator;
    private TrainingValidator trainingValidator;

    @Autowired
    public void setTrainingValidator(TrainingValidator trainingValidator){
        this.trainingValidator = trainingValidator;
    }
    @Autowired
    public void setIdGenerator(IdGenerator idGenerator){
        this.idGenerator = idGenerator;
    }
    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
        log.debug("TrainingDao injected into TrainingService");
    }

    @PostConstruct
    public void initialize(){
        idGenerator.initialize(
                trainingDao.findAll().stream()
                        .collect(Collectors.toMap(Training::getId, t -> t))
        );
        log.debug("IdGenerator initialized with existing training IDs");
    }

    public Training createTraining(Long traineeId, Long trainerId, String trainingName,
                                   TrainingType trainingType, LocalDate trainingDate,
                                   Integer duration) {
        log.info("Creating Training session: {} for trainee {} with trainer {}",
                trainingName, traineeId, trainerId);


        trainingValidator.validateTraining(traineeId, trainerId, trainingName, trainingType,
                trainingDate, duration);


        Training training = new Training();
        training.setId(idGenerator.generateNextId());
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(duration);


        Training savedTraining = trainingDao.save(training);
        log.info("Successfully created Training with ID: {}", savedTraining.getId());

        return savedTraining;
    }


    public Training selectTraining(Long id) {
        log.info("Selecting Training with ID: {}", id);

        if (id == null) {
            log.error("Training ID cannot be null");
            throw new IllegalArgumentException("Training ID cannot be null");
        }

        Training training = trainingDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Training not found with ID: {}", id);
                    return new TrainingNotFoundException("Training not found with id: " + id);
                });

        log.debug("Found Training: {} (name: {})", training.getId(), training.getTrainingName());
        return training;
    }


    public List<Training> selectAllTrainings() {
        log.info("Selecting all Trainings");

        List<Training> trainings = trainingDao.findAll();
        log.info("Found {} trainings", trainings.size());

        return trainings;
    }


    public List<Training> selectTrainingsByTraineeId(Long traineeId) {
        log.info("Selecting Trainings for trainee: {}", traineeId);

        if (traineeId == null) {
            log.error("Trainee ID cannot be null");
            throw new IllegalArgumentException("Trainee ID cannot be null");
        }

        List<Training> trainings = trainingDao.findByTraineeId(traineeId);
        log.info("Found {} trainings for trainee {}", trainings.size(), traineeId);

        return trainings;
    }


    public List<Training> selectTrainingsByTrainerId(Long trainerId) {
        log.info("Selecting Trainings for trainer: {}", trainerId);

        if (trainerId == null) {
            log.error("Trainer ID cannot be null");
            throw new IllegalArgumentException("Trainer ID cannot be null");
        }

        List<Training> trainings = trainingDao.findByTrainerId(trainerId);
        log.info("Found {} trainings for trainer {}", trainings.size(), trainerId);

        return trainings;
    }
}