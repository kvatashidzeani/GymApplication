package com.gymcrm.service;

import com.gymcrm.dao.TraineeDao;
import com.gymcrm.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private TraineeDao traineeDao;
    private UserCredentialGenerator credentialGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setCredentialGenerator(UserCredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    @Override
    public Trainee createTrainee(Trainee trainee) {
        logger.info("Creating trainee: {} {}", trainee.getFirstName(), trainee.getLastName());

        long duplicateCount = traineeDao.countByFirstAndLastName(
                trainee.getFirstName(), trainee.getLastName());

        String username = credentialGenerator.generateUsername(
                trainee.getFirstName(), trainee.getLastName(), duplicateCount);
        String password = credentialGenerator.generatePassword();

        trainee.setUsername(username);
        trainee.setPassword(password);
        trainee.setActive(true);

        Trainee saved = traineeDao.save(trainee);
        logger.info("Trainee created successfully with username: {}", saved.getUsername());
        return saved;
    }

    @Override
    public Trainee updateTrainee(Trainee trainee) {
        logger.info("Updating trainee with userId: {}", trainee.getUserId());
        Trainee updated = traineeDao.update(trainee);
        logger.info("Trainee updated successfully: {}", updated.getUsername());
        return updated;
    }

    @Override
    public void deleteTrainee(Long id) {
        logger.info("Deleting trainee with id: {}", id);
        traineeDao.deleteById(id);
        logger.info("Trainee deleted, id: {}", id);
    }

    @Override
    public Optional<Trainee> selectTrainee(Long id) {
        logger.debug("Selecting trainee with id: {}", id);
        Optional<Trainee> trainee = traineeDao.findById(id);
        if (trainee.isPresent()) {
            logger.debug("Trainee found: {}", trainee.get().getUsername());
        } else {
            logger.warn("Trainee not found with id: {}", id);
        }
        return trainee;
    }

    @Override
    public List<Trainee> selectAllTrainees() {
        logger.debug("Selecting all trainees");
        List<Trainee> trainees = traineeDao.findAll();
        logger.debug("Found {} trainees", trainees.size());
        return trainees;
    }
}