package com.gym.crm.service;

import com.gym.crm.exceptions.TrainerNotFoundException;
import com.gym.crm.Util.IdGenerator;
import com.gym.crm.Util.PasswordGenerator;
import com.gym.crm.Util.UsernameGenerator;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.validators.TrainerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private IdGenerator idGenerator;
    private TrainerValidator trainervalidator;

    @Autowired
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
        log.debug("TrainerDao injected into TrainerService");
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
        log.debug("UsernameGenerator injected into TrainerService");
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
        log.debug("PasswordGenerator injected into TrainerService");
    }

    @Autowired
    public void setTrainerValidator(TrainerValidator trainervalidator) {
        this.trainervalidator = trainervalidator;
    }

    @PostConstruct
    public void initIdGenerator() {
        idGenerator.initialize(
                trainerDao.findAll().stream()
                        .collect(Collectors.toMap(User::getId, t -> t))
        );
        log.debug("IdGenerator initialized with existing trainer IDs");
    }


    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        log.info("Creating Trainer profile: {} {}", firstName, lastName);
        trainervalidator.validateTrainer(firstName, lastName, specialization);

        String username = usernameGenerator.generateUsername(firstName, lastName);
        log.info("Generated username: {}", username);

        String password = passwordGenerator.generatePassword();
        log.debug("Generated password (length: {})", password.length());

        Trainer trainer = new Trainer();
        trainer.setId(idGenerator.generateNextId());
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setUsername(username);
        trainer.setPassword(password);
        trainer.setActive(true);
        trainer.setSpecialization(specialization);

        Trainer savedTrainer = trainerDao.save(trainer);
        log.info("Successfully created Trainer with ID: {} and username: {}",
                savedTrainer.getId(), savedTrainer.getUsername());

        return savedTrainer;
    }


    public Trainer updateTrainer(Long id, String firstName, String lastName,
                                 TrainingType specialization, Boolean isActive) {
        log.info("Updating Trainer profile with ID: {}", id);

        if (id == null) {
            log.error("Trainer ID cannot be null for update");
            throw new IllegalArgumentException("Trainer ID cannot be null");
        }

        Trainer trainer = trainerDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainer not found with ID: {}", id);
                    return new TrainerNotFoundException("Trainer not found with id: " + id);
                });
        trainervalidator.validateTrainer(firstName, lastName, specialization);
        log.debug("Found existing Trainer: {} (current username: {})",
                trainer.getId(), trainer.getUsername());

        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setSpecialization(specialization);

        if (isActive != null) {
            trainer.setActive(isActive);
        }

        log.debug("Username remains: {}", trainer.getUsername());
        log.debug("Password remains unchanged");

        Trainer updatedTrainer = trainerDao.update(trainer);
        log.info("Successfully updated Trainer with ID: {}", id);

        return updatedTrainer;
    }


    public Trainer selectTrainer(Long id) {
        log.info("Selecting Trainer with ID: {}", id);

        if (id == null) {
            log.error("Trainer ID cannot be null");
            throw new IllegalArgumentException("Trainer ID cannot be null");
        }

        Trainer trainer = trainerDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainer not found with ID: {}", id);
                    return new TrainerNotFoundException("Trainer not found with id: " + id);
                });

        log.debug("Found Trainer: {} (username: {})", trainer.getId(), trainer.getUsername());
        return trainer;
    }

    public List<Trainer> selectAllTrainers() {
        log.info("Selecting all Trainers");

        List<Trainer> trainers = trainerDao.findAll();
        log.info("Found {} trainers", trainers.size());

        return trainers;
    }

    public Trainer selectTrainerByUsername(String username) {
        log.info("Selecting Trainer with username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            log.error("Username cannot be null or empty");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        List<Trainer> allTrainers = trainerDao.findAll();
        Trainer trainer = allTrainers.stream()
                .filter(t -> t.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Trainer not found with username: {}", username);
                    return new TrainerNotFoundException("Trainer not found with username: " + username);
                });

        log.debug("Found Trainer with ID: {}", trainer.getId());
        return trainer;
    }
}