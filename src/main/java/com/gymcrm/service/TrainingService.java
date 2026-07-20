package com.gymcrm.service;

import com.gymcrm.exceptions.TrainingNotFoundException;
import com.gymcrm.Util.IdGenerator;
import com.gymcrm.dao.TrainingDao;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.storage.TrainingTypeStorage;
import com.gymcrm.validators.TrainingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);


    private TrainingDao trainingDao;
    private IdGenerator idGenerator;
    private TrainingValidator trainingValidator;
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingTypeStorage trainingTypeStorage;

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

    @Autowired
    public void setTraineeService(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Autowired
    public void setTrainerService(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Autowired
    public void setTrainingTypeStorage(TrainingTypeStorage trainingTypeStorage) {
        this.trainingTypeStorage = trainingTypeStorage;
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

    /**
     * Add training by trainee/trainer usernames and training type name.
     */
    public Training addTraining(String traineeUsername,
                                String trainerUsername,
                                String trainingName,
                                String trainingTypeName,
                                LocalDate trainingDate,
                                Integer duration) {
        log.info("Adding training '{}' for trainee {} with trainer {}",
                trainingName, traineeUsername, trainerUsername);

        if (traineeUsername == null || traineeUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Trainee username cannot be null or empty");
        }
        if (trainerUsername == null || trainerUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Trainer username cannot be null or empty");
        }
        if (trainingTypeName == null || trainingTypeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Training type cannot be null or empty");
        }

        Long traineeId = traineeService.selectTraineeByUsername(traineeUsername).getId();
        Long trainerId = trainerService.selectTrainerByUsername(trainerUsername).getId();

        TrainingType trainingType = trainingTypeStorage.requireByName(trainingTypeName);
        return createTraining(traineeId, trainerId, trainingName, trainingType, trainingDate, duration);
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
