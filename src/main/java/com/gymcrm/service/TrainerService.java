package com.gymcrm.service;

import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.Util.IdGenerator;
import com.gymcrm.Util.PasswordGenerator;
import com.gymcrm.Util.UsernameGenerator;
import com.gymcrm.dao.TraineeDao;
import com.gymcrm.dao.TrainerDao;
import com.gymcrm.dao.TrainingDao;
import com.gymcrm.dao.UserDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.validators.TrainerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private TrainerDao trainerDao;
    private TraineeDao traineeDao;
    private TrainingDao trainingDao;
    private UserDao userDao;
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
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
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
                        .collect(Collectors.toMap(Trainer::getId, t -> t))
        );
        log.debug("IdGenerator initialized with existing trainer IDs");
    }

    /**
     * Create Trainer profile per DB schema:
     * 1) create User row
     * 2) create Trainer row with userId FK + specialization
     */
    public Trainer createTrainer(String firstName, String lastName, TrainingType specialization) {
        log.info("Creating Trainer profile: {} {}", firstName, lastName);
        trainervalidator.validateTrainer(firstName, lastName, specialization);

        String username = usernameGenerator.generateUsername(firstName, lastName);
        String password = passwordGenerator.generatePassword();
        log.info("Generated username: {}", username);

        User user = new User();
        user.setUserId(idGenerator.generateNextId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);
        userDao.save(user);

        Trainer trainer = new Trainer();
        trainer.setId(idGenerator.generateNextId());
        trainer.setSpecialization(specialization);
        trainer.setUserId(user.getUserId());
        trainer.setUser(user);

        Trainer savedTrainer = trainerDao.save(trainer);
        log.info("Successfully created Trainer: id={}, userId={}, username={}",
                savedTrainer.getId(), savedTrainer.getUserId(), username);

        return savedTrainer;
    }

    public Trainer updateTrainer(Long id, String firstName, String lastName,
                                 TrainingType specialization, Boolean isActive) {
        log.info("Updating Trainer profile with ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Trainer ID cannot be null");
        }

        Trainer trainer = trainerDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainer not found with ID: {}", id);
                    return new TrainerNotFoundException("Trainer not found with id: " + id);
                });
        trainervalidator.validateTrainer(firstName, lastName, specialization);

        User user = userDao.findById(trainer.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for trainer id: " + id));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        if (isActive != null) {
            user.setActive(isActive);
        }
        userDao.update(user);

        trainer.setSpecialization(specialization);
        trainer.setUser(user);

        Trainer updatedTrainer = trainerDao.update(trainer);
        log.info("Successfully updated Trainer with ID: {}", id);
        return updatedTrainer;
    }

    /**
     * Activate or de-activate Trainer (updates User.isActive via userId FK).
     */
    public Trainer setTrainerActive(Long id, boolean isActive) {
        log.info("Setting trainer id={} active={}", id, isActive);

        if (id == null) {
            throw new IllegalArgumentException("Trainer ID cannot be null");
        }

        Trainer trainer = trainerDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainer not found with ID: {}", id);
                    return new TrainerNotFoundException("Trainer not found with id: " + id);
                });

        User user = userDao.findById(trainer.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for trainer id: " + id));

        user.setActive(isActive);
        userDao.update(user);
        trainer.setUser(user);

        log.info("Trainer id={} is now active={}", id, isActive);
        return trainer;
    }

    public Trainer selectTrainer(Long id) {
        log.info("Selecting Trainer with ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Trainer ID cannot be null");
        }

        Trainer trainer = trainerDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainer not found with ID: {}", id);
                    return new TrainerNotFoundException("Trainer not found with id: " + id);
                });
        attachUser(trainer);
        return trainer;
    }

    public List<Trainer> selectAllTrainers() {
        log.info("Selecting all Trainers");
        List<Trainer> trainers = trainerDao.findAll();
        trainers.forEach(this::attachUser);
        log.info("Found {} trainers", trainers.size());
        return trainers;
    }

    public Trainer selectTrainerByUsername(String username) {
        log.info("Selecting Trainer with username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userDao.findAll().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with username: " + username));

        Trainer trainer = trainerDao.findAll().stream()
                .filter(t -> user.getUserId().equals(t.getUserId()))
                .findFirst()
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with username: " + username));

        trainer.setUser(user);
        return trainer;
    }

    /**
     * Get trainer trainings by username with optional filters.
     * Null criteria are ignored (no filter applied).
     */
    public List<Training> getTrainerTrainingsList(String username,
                                                  LocalDate fromDate,
                                                  LocalDate toDate,
                                                  String traineeName) {
        log.info("Getting trainings for trainer {} with criteria from={}, to={}, traineeName={}",
                username, fromDate, toDate, traineeName);

        Trainer trainer = selectTrainerByUsername(username);
        List<Training> trainings = trainingDao.findByTrainerId(trainer.getId()).stream()
                .filter(t -> fromDate == null || !t.getTrainingDate().isBefore(fromDate))
                .filter(t -> toDate == null || !t.getTrainingDate().isAfter(toDate))
                .filter(t -> matchesTraineeName(t, traineeName))
                .collect(Collectors.toList());

        log.info("Found {} trainings for trainer {} after filtering", trainings.size(), username);
        return trainings;
    }

    private boolean matchesTraineeName(Training training, String traineeName) {
        if (traineeName == null || traineeName.trim().isEmpty()) {
            return true;
        }

        Trainee trainee = traineeDao.findById(training.getTraineeId()).orElse(null);
        if (trainee == null || trainee.getUserId() == null) {
            return false;
        }

        User traineeUser = userDao.findById(trainee.getUserId()).orElse(null);
        if (traineeUser == null) {
            return false;
        }

        String needle = traineeName.trim().toLowerCase(Locale.ROOT);
        String first = traineeUser.getFirstName() == null ? "" : traineeUser.getFirstName().toLowerCase(Locale.ROOT);
        String last = traineeUser.getLastName() == null ? "" : traineeUser.getLastName().toLowerCase(Locale.ROOT);
        String full = (first + " " + last).trim();
        String uname = traineeUser.getUsername() == null ? "" : traineeUser.getUsername().toLowerCase(Locale.ROOT);

        return needle.equals(first)
                || needle.equals(last)
                || needle.equals(full)
                || needle.equals(uname);
    }

    /**
     * Match trainer username and password against the linked User credentials.
     * @return true if a trainer exists for that username and the password matches
     */
    public boolean matchTrainerCredentials(String username, String password) {
        log.info("Matching trainer credentials for username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        User user = userDao.findAll().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElse(null);

        if (user == null) {
            log.warn("No user found with username: {}", username);
            return false;
        }

        boolean isTrainer = trainerDao.findAll().stream()
                .anyMatch(t -> user.getUserId().equals(t.getUserId()));

        if (!isTrainer) {
            log.warn("User {} exists but is not a trainer", username);
            return false;
        }

        boolean matches = password.equals(user.getPassword());
        log.info("Trainer credential match for {}: {}", username, matches);
        return matches;
    }

    /**
     * Change Trainer password (updates User.password via userId FK).
     * Verifies oldPassword first.
     */
    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for trainer username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new IllegalArgumentException("Old password cannot be null or empty");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }

        if (!matchTrainerCredentials(username, oldPassword)) {
            log.error("Old password does not match for trainer: {}", username);
            throw new IllegalArgumentException("Old password is incorrect");
        }

        User user = userDao.findAll().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with username: " + username));

        user.setPassword(newPassword);
        userDao.update(user);
        log.info("Password changed successfully for trainer: {}", username);
    }

    private void attachUser(Trainer trainer) {
        if (trainer.getUserId() != null) {
            userDao.findById(trainer.getUserId()).ifPresent(trainer::setUser);
        }
    }
}
