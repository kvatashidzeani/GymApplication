package com.gymcrm.service;

import com.gymcrm.dao.TrainerDao;
import com.gymcrm.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDao trainerDao;
    private UserCredentialGenerator credentialGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setCredentialGenerator(UserCredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    @Override
    public Trainer createTrainer(Trainer trainer) {
        logger.info("Creating trainer: {} {}", trainer.getFirstName(), trainer.getLastName());

        long duplicateCount = trainerDao.countByFirstAndLastName(
                trainer.getFirstName(), trainer.getLastName());

        String username = credentialGenerator.generateUsername(
                trainer.getFirstName(), trainer.getLastName(), duplicateCount);
        String password = credentialGenerator.generatePassword();

        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setActive(true);

        Trainer saved = trainerDao.save(trainer);
        logger.info("Trainer created successfully with username: {}", saved.getUsername());
        return saved;
    }

    @Override
    public Trainer updateTrainer(Trainer trainer) {
        logger.info("Updating trainer with userId: {}", trainer.getUserId());
        Trainer updated = trainerDao.update(trainer);
        logger.info("Trainer updated successfully: {}", updated.getUsername());
        return updated;
    }

    @Override
    public Optional<Trainer> selectTrainer(Long id) {
        logger.debug("Selecting trainer with id: {}", id);
        Optional<Trainer> trainer = trainerDao.findById(id);
        if (trainer.isPresent()) {
            logger.debug("Trainer found: {}", trainer.get().getUsername());
        } else {
            logger.warn("Trainer not found with id: {}", id);
        }
        return trainer;
    }

    @Override
    public List<Trainer> selectAllTrainers() {
        logger.debug("Selecting all trainers");
        List<Trainer> trainers = trainerDao.findAll();
        logger.debug("Found {} trainers", trainers.size());
        return trainers;
    }
}