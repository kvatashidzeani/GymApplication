package com.gymcrm.service;

import com.gymcrm.dao.TrainingDao;
import com.gymcrm.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public Training createTraining(Training training) {
        logger.info("Creating training: {}", training.getTrainingName());
        Training saved = trainingDao.save(training);
        logger.info("Training created successfully: id={}, name={}", saved.getId(), saved.getTrainingName());
        return saved;
    }

    @Override
    public Optional<Training> selectTraining(Long id) {
        logger.debug("Selecting training with id: {}", id);
        Optional<Training> training = trainingDao.findById(id);
        if (training.isPresent()) {
            logger.debug("Training found: {}", training.get().getTrainingName());
        } else {
            logger.warn("Training not found with id: {}", id);
        }
        return training;
    }

    @Override
    public List<Training> selectAllTrainings() {
        logger.debug("Selecting all trainings");
        List<Training> trainings = trainingDao.findAll();
        logger.debug("Found {} trainings", trainings.size());
        return trainings;
    }
}