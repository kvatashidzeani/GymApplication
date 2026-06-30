package com.gym.crm.dao.impl;

import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import com.gym.crm.storage.TrainingStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private TrainingStorage trainingStorage;
    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);


    @Autowired
    public void setTrainingStorage(TrainingStorage trainingStorage){
        this.trainingStorage = trainingStorage;
    }

    @Override
    public Training save(Training training) {
        log.info("Saving a training");
        if (training == null || training.getId() == null) {
            log.error("The training or the training id passed in is null.");
            throw new IllegalArgumentException("The training or the training id passed in is null.");
        }
        trainingStorage.getStorage().put(training.getId(), training);
        log.info("Saved the training successfully with ID: {}", training.getId());
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Optional<Training> training = Optional.ofNullable(trainingStorage.getStorage().get(id));
        training.ifPresentOrElse(t ->
            log.info("Training found with id {}",id),
                () -> log.warn("Training NOT found with id {}", id
        ));
        return training;
    }

    @Override
    public List<Training> findAll() {
        log.info("Attempting to collect every training");
        ArrayList<Training> all = new ArrayList<>(trainingStorage.getStorage().values());
        log.info("Successfully found {} trainings", all.size());
        return all;
    }

    @Override
    public void delete(Long id) {
        trainingStorage.getStorage().remove(id);
        log.debug("Deleted training with id {}. Total trainings: {}", id, trainingStorage.getStorage().size());
    }

    @Override
    public List<Training> findByTrainerId(Long trainerId) {
        log.info("Finding trainings for trainer with id {}", trainerId);

        List<Training> trainings = trainingStorage.getStorage().values().stream()
                .filter(training -> training.getTrainerId().equals(trainerId))
                .collect(Collectors.toList());

        log.info("Found {} trainings for trainer with id {}", trainings.size(), trainerId);
        return trainings;
    }

    @Override
    public List<Training> findByTraineeId(Long traineeId) {
        log.info("Finding trainings for trainee with id {}", traineeId);

        List<Training> trainings = trainingStorage.getStorage().values().stream()
                .filter(training -> training.getTraineeId().equals(traineeId))
                .collect(Collectors.toList());

        log.info("Found {} trainings for trainee with id {}", trainings.size(), traineeId);
        return trainings;
    }
}