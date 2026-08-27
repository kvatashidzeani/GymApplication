package com.gymcrm.service;

import com.gymcrm.client.WorkloadClient;
import com.gymcrm.exceptions.TraineeNotFoundException;
import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.Util.IdGenerator;
import com.gymcrm.Util.PasswordGenerator;
import com.gymcrm.Util.UsernameGenerator;
import com.gymcrm.dao.TrainingDao;
import com.gymcrm.dao.UserDao;
import com.gymcrm.dao.impl.TraineeDaoImpl;
import com.gymcrm.dao.impl.TrainerDaoImpl;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.User;
import com.gymcrm.validators.TraineeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private PasswordGenerator passwordGenerator;
    private UsernameGenerator usernameGenerator;
    private TraineeDaoImpl traineeDao;
    private TrainerDaoImpl trainerDao;
    private TrainingDao trainingDao;
    private UserDao userDao;
    private IdGenerator idGenerator;
    private TraineeValidator traineeValidator;
    private PasswordEncoder passwordEncoder;
    private WorkloadClient workloadClient;

    @Autowired
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        log.debug("IdGenerator injected into TraineeService");
    }

    @Autowired
    public void setTraineeValidator(TraineeValidator traineeValidator) {
        this.traineeValidator = traineeValidator;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDaoImpl trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setWorkloadClient(WorkloadClient workloadClient) {
        this.workloadClient = workloadClient;
    }

    @PostConstruct
    public void initIdGenerator() {
        idGenerator.initialize(
                traineeDao.findAll().stream()
                        .collect(Collectors.toMap(Trainee::getId, t -> t))
        );
        log.debug("IdGenerator initialized with existing trainee IDs");
    }

    @Autowired
    public void setTraineeDao(TraineeDaoImpl traineeDao) {
        this.traineeDao = traineeDao;
        log.debug("TraineeDaoImpl injected into TraineeService");
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
        log.debug("UsernameGenerator injected into TraineeService");
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
        log.debug("PasswordGenerator injected into TraineeService");
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        log.debug("PasswordEncoder injected into TraineeService");
    }

    /**
     * Create Trainee profile per DB schema:
     * 1) create User row
     * 2) create Trainee row with userId FK
     */
    @Transactional(rollbackFor = Exception.class)
    public Trainee createTrainee(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        log.info("Creating a new Trainee profile: {} {}", firstName, lastName);

        traineeValidator.validateTrainee(firstName, lastName, dateOfBirth, address);
        ensureNotRegisteredAsOtherRole(firstName, lastName);

        String username = usernameGenerator.generateUsername(firstName, lastName);
        String rawPassword = passwordGenerator.generatePassword();
        log.info("Generated username: {}", username);

        User user = new User();
        user.setUserId(idGenerator.generateNextId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        userDao.save(user);

        Trainee trainee = new Trainee();
        trainee.setId(idGenerator.generateNextId());
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setUserId(user.getUserId());
        trainee.setUser(user);

        Trainee saved = traineeDao.save(trainee);
        // Expose plain password once for registration response (not stored)
        saved.getUser().setRawPassword(rawPassword);
        log.info("Trainee created: id={}, userId={}, username={}",
                saved.getId(), saved.getUserId(), username);

        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public Trainee updateTrainee(Long id, String firstName, String lastName,
                                 LocalDate dateOfBirth, String address, Boolean isActive) {
        log.info("Updating trainee id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("Trainee ID cannot be null");
        }
        traineeValidator.validateTrainee(firstName, lastName, dateOfBirth, address);

        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainee not found with id: {}", id);
                    return new TraineeNotFoundException();
                });

        User user = userDao.findById(trainee.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for trainee id: " + id));

        // Username is immutable — never updated here.
        user.setFirstName(firstName);
        user.setLastName(lastName);
        if (isActive != null) {
            user.setActive(isActive);
        }
        userDao.update(user);

        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);
        trainee.setUser(user);

        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee updated successfully: {}", id);
        return updated;
    }

    /**
     * Activate or de-activate Trainee (updates User.isActive via userId FK).
     * Non-idempotent: fails if the profile is already in the requested state.
     */
    public Trainee setTraineeActive(Long id, boolean isActive) {
        log.info("Setting trainee id={} active={}", id, isActive);

        if (id == null) {
            throw new IllegalArgumentException("Trainee ID cannot be null");
        }

        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainee not found with id: {}", id);
                    return new TraineeNotFoundException();
                });

        User user = userDao.findById(trainee.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for trainee id: " + id));

        if (user.isActive() == isActive) {
            log.error("Trainee id={} is already {}", id, isActive ? "active" : "inactive");
            throw new IllegalStateException("Trainee is already " + (isActive ? "active" : "inactive"));
        }

        user.setActive(isActive);
        userDao.update(user);
        trainee.setUser(user);

        log.info("Trainee id={} is now active={}", id, isActive);
        return trainee;
    }

    /**
     * Hard-delete Trainee profile: removes linked User and cascades deletion of all trainings.
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTrainee(Long id) {
        doDeleteTrainee(id);
    }

    /**
     * Hard-delete Trainee profile by username (Trainee + User + cascade trainings).
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTraineeByUsername(String username) {
        log.info("Deleting trainee by username: {}", username);
        Trainee trainee = selectTraineeByUsername(username);
        doDeleteTrainee(trainee.getId());
    }

    private void doDeleteTrainee(Long id) {
        log.info("Hard-deleting trainee id={}", id);

        if (id == null) {
            throw new IllegalArgumentException("Trainee ID cannot be null");
        }

        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainee not found with id: {}", id);
                    return new TraineeNotFoundException();
                });

        Long userId = trainee.getUserId();
        unlinkTraineeFromAllTrainers(trainee);

        List<Training> trainings = trainingDao.findByTraineeId(id);
        trainings.forEach(training -> {
            notifyWorkloadDeleted(training);
            trainingDao.delete(training.getId());
        });
        log.info("Cascade-deleted {} training(s) for trainee id={}", trainings.size(), id);

        traineeDao.delete(id);
        if (userId != null) {
            userDao.delete(userId);
        }
        log.info("Hard-deleted trainee id={}, userId={}, cascade trainings={}",
                id, userId, trainings.size());
    }

    private void notifyWorkloadDeleted(Training training) {
        if (workloadClient == null || training == null || training.getTrainerId() == null) {
            return;
        }
        try {
            Trainer trainer = trainerDao.findById(training.getTrainerId()).orElse(null);
            if (trainer == null) {
                log.warn("Skip workload DELETE: trainer id={} not found for training id={}",
                        training.getTrainerId(), training.getId());
                return;
            }
            workloadClient.notifyTrainingDeleted(trainer, training);
        } catch (RuntimeException ex) {
            log.error("Workload DELETE notification failed for training id={}: {}",
                    training.getId(), ex.getMessage());
        }
    }

    /**
     * Get trainee trainings by username with optional filters.
     * Null criteria are ignored (no filter applied).
     */
    public List<Training> getTraineeTrainingsList(String username,
                                                  LocalDate fromDate,
                                                  LocalDate toDate,
                                                  String trainerName,
                                                  String trainingType) {
        log.info("Getting trainings for trainee {} with criteria from={}, to={}, trainerName={}, type={}",
                username, fromDate, toDate, trainerName, trainingType);

        Trainee trainee = selectTraineeByUsername(username);
        List<Training> trainings = trainingDao.findByTraineeId(trainee.getId()).stream()
                .filter(t -> fromDate == null || !t.getTrainingDate().isBefore(fromDate))
                .filter(t -> toDate == null || !t.getTrainingDate().isAfter(toDate))
                .filter(t -> matchesTrainingType(t, trainingType))
                .filter(t -> matchesTrainerName(t, trainerName))
                .collect(Collectors.toList());

        log.info("Found {} trainings for trainee {} after filtering", trainings.size(), username);
        return trainings;
    }

    /**
     * Get active trainers not yet assigned to the trainee.
     */
    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername) {
        log.info("Getting active trainers not assigned to trainee: {}", traineeUsername);

        if (traineeUsername == null || traineeUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Trainee username cannot be null or empty");
        }

        Trainee trainee = selectTraineeByUsername(traineeUsername);
        Set<Long> assignedTrainerIds = trainee.getTrainerIds();

        List<Trainer> unassignedTrainers = trainerDao.findAll().stream()
                .filter(trainer -> !assignedTrainerIds.contains(trainer.getId()))
                .peek(this::attachUserToTrainer)
                .filter(trainer -> trainer.getUser() != null && trainer.getUser().isActive())
                .collect(Collectors.toList());

        log.info("Found {} unassigned active trainers for trainee {}", unassignedTrainers.size(), traineeUsername);
        return unassignedTrainers;
    }

    /**
     * Update trainee's assigned trainers list (many-to-many) by trainer usernames.
     * Keeps both sides in sync: Trainee.trainerIds and Trainer.traineeIds.
     */
    public void updateTraineeTrainersList(String traineeUsername, List<String> trainerUsernames) {
        log.info("Updating trainers list for trainee: {}", traineeUsername);

        if (traineeUsername == null || traineeUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Trainee username cannot be null or empty");
        }
        if (trainerUsernames == null) {
            throw new IllegalArgumentException("Trainer usernames list cannot be null");
        }

        Trainee trainee = selectTraineeByUsername(traineeUsername);
        Set<Long> previousTrainerIds = new HashSet<>(trainee.getTrainerIds());
        Set<Long> trainerIds = new HashSet<>();
        for (String trainerUsername : trainerUsernames) {
            if (trainerUsername == null || trainerUsername.trim().isEmpty()) {
                throw new IllegalArgumentException("Trainer username cannot be null or empty");
            }
            trainerIds.add(resolveTrainerIdByUsername(trainerUsername.trim()));
        }

        syncTraineeTrainerAssignments(trainee, previousTrainerIds, trainerIds);
        log.info("Updated trainers list for trainee {}: {} trainer(s)", traineeUsername, trainerIds.size());
    }

    private void syncTraineeTrainerAssignments(Trainee trainee,
                                               Set<Long> previousTrainerIds,
                                               Set<Long> newTrainerIds) {
        Long traineeId = trainee.getId();

        for (Long oldTrainerId : previousTrainerIds) {
            if (!newTrainerIds.contains(oldTrainerId)) {
                trainerDao.findById(oldTrainerId).ifPresent(trainer -> {
                    trainer.getTraineeIds().remove(traineeId);
                    trainerDao.update(trainer);
                });
            }
        }

        for (Long newTrainerId : newTrainerIds) {
            trainerDao.findById(newTrainerId).ifPresent(trainer -> {
                trainer.getTraineeIds().add(traineeId);
                trainerDao.update(trainer);
            });
        }

        trainee.setTrainerIds(newTrainerIds);
        traineeDao.update(trainee);
    }

    private void unlinkTraineeFromAllTrainers(Trainee trainee) {
        if (trainee.getTrainerIds() == null || trainee.getTrainerIds().isEmpty()) {
            return;
        }
        Long traineeId = trainee.getId();
        for (Long trainerId : new HashSet<>(trainee.getTrainerIds())) {
            trainerDao.findById(trainerId).ifPresent(trainer -> {
                trainer.getTraineeIds().remove(traineeId);
                trainerDao.update(trainer);
            });
        }
        trainee.setTrainerIds(new HashSet<>());
    }

    private Long resolveTrainerIdByUsername(String username) {
        User user = userDao.findAll().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with username: " + username));

        return trainerDao.findAll().stream()
                .filter(t -> user.getUserId().equals(t.getUserId()))
                .findFirst()
                .map(Trainer::getId)
                .orElseThrow(() -> new TrainerNotFoundException("Trainer not found with username: " + username));
    }

    /**
     * A person cannot be registered as both trainee and trainer (same first + last name).
     */
    private void ensureNotRegisteredAsOtherRole(String firstName, String lastName) {
        for (Trainer trainer : trainerDao.findAll()) {
            if (trainer.getUserId() == null) {
                continue;
            }
            User user = userDao.findById(trainer.getUserId()).orElse(null);
            if (user != null && namesEqual(user.getFirstName(), firstName)
                    && namesEqual(user.getLastName(), lastName)) {
                log.error("Cannot register trainee: already registered as trainer {} {}", firstName, lastName);
                throw new IllegalArgumentException(
                        "Cannot register as trainee: already registered as trainer with the same first and last name");
            }
        }
    }

    private static boolean namesEqual(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private void attachUserToTrainer(Trainer trainer) {
        if (trainer.getUserId() != null) {
            userDao.findById(trainer.getUserId()).ifPresent(trainer::setUser);
        }
    }

    private boolean matchesTrainingType(Training training, String trainingType) {
        if (trainingType == null || trainingType.trim().isEmpty()) {
            return true;
        }
        if (training.getTrainingType() == null || training.getTrainingType().getTrainingTypeName() == null) {
            return false;
        }
        return trainingType.equalsIgnoreCase(training.getTrainingType().getTrainingTypeName());
    }

    private boolean matchesTrainerName(Training training, String trainerName) {
        if (trainerName == null || trainerName.trim().isEmpty()) {
            return true;
        }

        Trainer trainer = trainerDao.findById(training.getTrainerId()).orElse(null);
        if (trainer == null || trainer.getUserId() == null) {
            return false;
        }

        User trainerUser = userDao.findById(trainer.getUserId()).orElse(null);
        if (trainerUser == null) {
            return false;
        }

        String needle = trainerName.trim().toLowerCase(Locale.ROOT);
        String first = trainerUser.getFirstName() == null ? "" : trainerUser.getFirstName().toLowerCase(Locale.ROOT);
        String last = trainerUser.getLastName() == null ? "" : trainerUser.getLastName().toLowerCase(Locale.ROOT);
        String full = (first + " " + last).trim();
        String uname = trainerUser.getUsername() == null ? "" : trainerUser.getUsername().toLowerCase(Locale.ROOT);

        return needle.equals(first)
                || needle.equals(last)
                || needle.equals(full)
                || needle.equals(uname);
    }

    public Trainee select(Long id) {
        log.info("Selecting trainee with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Trainee ID cannot be null");
        }

        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.error("Trainee not found with id: {}", id);
                    return new TraineeNotFoundException();
                });
        attachUser(trainee);
        return trainee;
    }

    public List<Trainee> selectAllTrainees() {
        log.info("Selecting all trainees");
        List<Trainee> trainees = traineeDao.findAll();
        trainees.forEach(this::attachUser);
        log.info("Found {} trainees", trainees.size());
        return trainees;
    }

    /**
     * Select Trainee profile by username (via User table, then Trainee.userId FK).
     */
    public Trainee selectTraineeByUsername(String username) {
        log.info("Selecting Trainee with username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userDao.findAll().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Trainee not found with username: {}", username);
                    return new TraineeNotFoundException("Trainee not found with username: " + username);
                });

        Trainee trainee = traineeDao.findAll().stream()
                .filter(t -> user.getUserId().equals(t.getUserId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("User {} exists but is not a trainee", username);
                    return new TraineeNotFoundException("Trainee not found with username: " + username);
                });

        trainee.setUser(user);
        log.info("Found Trainee id={} for username={}", trainee.getId(), username);
        return trainee;
    }

    /**
     * Match trainee username and password against the linked User credentials.
     * @return true if a trainee exists for that username and the password matches
     */
    public boolean matchTraineeCredentials(String username, String password) {
        log.info("Matching trainee credentials for username: {}", username);

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

        boolean isTrainee = traineeDao.findAll().stream()
                .anyMatch(t -> user.getUserId().equals(t.getUserId()));

        if (!isTrainee) {
            log.warn("User {} exists but is not a trainee", username);
            return false;
        }

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        log.info("Trainee credential match for {}: {}", username, matches);
        return matches;
    }

    /**
     * Change Trainee password (updates User.password via userId FK).
     * Verifies oldPassword first.
     */
    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for trainee username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new IllegalArgumentException("Old password cannot be null or empty");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }

        if (!matchTraineeCredentials(username, oldPassword)) {
            log.error("Old password does not match for trainee: {}", username);
            throw new IllegalArgumentException("Old password is incorrect");
        }

        User user = userDao.findAll().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst()
                .orElseThrow(() -> new TraineeNotFoundException("Trainee not found with username: " + username));

        user.setPassword(passwordEncoder.encode(newPassword));
        userDao.update(user);
        log.info("Password changed successfully for trainee: {}", username);
    }

    private void attachUser(Trainee trainee) {
        if (trainee.getUserId() != null) {
            userDao.findById(trainee.getUserId()).ifPresent(trainee::setUser);
        }
    }
}
