package com.gymcrm.service;

import com.gymcrm.Util.IdGenerator;
import com.gymcrm.Util.PasswordGenerator;
import com.gymcrm.Util.UsernameGenerator;
import com.gymcrm.dao.TrainingDao;
import com.gymcrm.dao.UserDao;
import com.gymcrm.dao.impl.TraineeDaoImpl;
import com.gymcrm.dao.impl.TrainerDaoImpl;
import com.gymcrm.exceptions.TraineeNotFoundException;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.validators.TraineeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    private TraineeService traineeService;
    private TraineeDaoImpl traineeDao;
    private TrainerDaoImpl trainerDao;
    private TrainingDao trainingDao;
    private UserDao userDao;
    private IdGenerator idGenerator;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TraineeValidator traineeValidator;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        traineeDao = mock(TraineeDaoImpl.class);
        trainerDao = mock(TrainerDaoImpl.class);
        trainingDao = mock(TrainingDao.class);
        userDao = mock(UserDao.class);
        idGenerator = mock(IdGenerator.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        traineeValidator = mock(TraineeValidator.class);
        passwordEncoder = mock(PasswordEncoder.class);

        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setTrainerDao(trainerDao);
        traineeService.setTrainingDao(trainingDao);
        traineeService.setUserDao(userDao);
        traineeService.setIdGenerator(idGenerator);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
        traineeService.setTraineeValidator(traineeValidator);
        traineeService.setPasswordEncoder(passwordEncoder);
    }

    @Test
    void createTrainee_savesUserAndTrainee() {
        when(idGenerator.generateNextId()).thenReturn(1L, 2L);
        when(usernameGenerator.generateUsername("Ani", "Smith")).thenReturn("Ani.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("secret");
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");
        when(trainerDao.findAll()).thenReturn(List.of());
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = traineeService.createTrainee(
                "Ani", "Smith", LocalDate.of(2000, 1, 1), "Tbilisi");

        assertEquals(2L, result.getId());
        assertEquals(1L, result.getUserId());
        assertNotNull(result.getUser());
        assertEquals("Ani.Smith", result.getUser().getUsername());
        assertEquals("hashed-secret", result.getUser().getPassword());
        assertEquals("secret", result.getUser().getRawPassword());
        verify(passwordEncoder).encode("secret");
        verify(userDao).save(any(User.class));
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void createTrainee_rejectsIfAlreadyTrainer() {
        User trainerUser = new User("Ani", "Smith", "Ani.Smith", "x", true, 5L);
        Trainer trainer = new Trainer(10L, null, 5L);
        when(trainerDao.findAll()).thenReturn(List.of(trainer));
        when(userDao.findById(5L)).thenReturn(Optional.of(trainerUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> traineeService.createTrainee("Ani", "Smith", null, null));

        assertTrue(ex.getMessage().contains("already registered as trainer"));
        verify(traineeDao, never()).save(any());
    }

    @Test
    void createTrainee_invalidInput_throws() {
        doThrow(new IllegalArgumentException("Invalid trainee"))
                .when(traineeValidator)
                .validateTrainee(any(), any(), any(), any());

        assertThrows(IllegalArgumentException.class, () ->
                traineeService.createTrainee(null, "Smith", LocalDate.of(2000, 1, 1), "Tbilisi"));
    }

    @Test
    void select_existingTrainee() {
        Trainee trainee = new Trainee(5L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(traineeDao.findById(5L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.select(5L);

        assertEquals(5L, result.getId());
    }

    @Test
    void select_notFound_throws() {
        when(traineeDao.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TraineeNotFoundException.class, () -> traineeService.select(1L));
    }

    @Test
    void updateTrainee_updatesUserAndTrainee() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Old", 10L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(userDao.findById(10L)).thenReturn(Optional.of(user));
        when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee updated = traineeService.updateTrainee(
                1L, "Ani", "Kvatashidze", LocalDate.of(2001, 2, 2), "Gora", false);

        assertEquals("Gora", updated.getAddress());
        assertEquals("Kvatashidze", user.getLastName());
        assertFalse(user.isActive());
        verify(userDao).update(user);
    }

    @Test
    void deleteTrainee_cascadesTrainingsAndUser() {
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        Training training = new Training();
        training.setId(100L);

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainingDao.findByTraineeId(1L)).thenReturn(List.of(training));

        traineeService.deleteTrainee(1L);

        verify(trainingDao).delete(100L);
        verify(traineeDao).delete(1L);
        verify(userDao).delete(10L);
    }

    @Test
    void setTraineeActive_whenAlreadyActive_throws() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(userDao.findById(10L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> traineeService.setTraineeActive(1L, true));
    }

    @Test
    void matchTraineeCredentials_validCredentials_returnsTrue() {
        User user = new User("Ani", "Smith", "Ani.Smith", "hashed-secret", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));
        when(passwordEncoder.matches("secret", "hashed-secret")).thenReturn(true);

        assertTrue(traineeService.matchTraineeCredentials("Ani.Smith", "secret"));
    }

    @Test
    void matchTraineeCredentials_wrongPassword_returnsFalse() {
        User user = new User("Ani", "Smith", "Ani.Smith", "hashed-secret", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));
        when(passwordEncoder.matches("wrong", "hashed-secret")).thenReturn(false);

        assertFalse(traineeService.matchTraineeCredentials("Ani.Smith", "wrong"));
    }

    @Test
    void changeTraineePassword_success() {
        User user = new User("Ani", "Smith", "Ani.Smith", "hashed-old", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));
        when(passwordEncoder.matches("old", "hashed-old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("hashed-new");

        traineeService.changeTraineePassword("Ani.Smith", "old", "new");

        assertEquals("hashed-new", user.getPassword());
        verify(userDao).update(user);
        verify(passwordEncoder).encode("new");
    }

    @Test
    void select_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.select(null));
    }

    @Test
    void updateTraineeTrainersList_syncsBothSides() {
        User traineeUser = new User("Ani", "Smith", "Ani.Smith", "pass", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        trainee.setTrainerIds(Set.of(5L));

        User oldTrainerUser = new User("Old", "Trainer", "Old.Trainer", "x", true, 50L);
        Trainer oldTrainer = new Trainer(5L, null, 50L);
        oldTrainer.getTraineeIds().add(1L);

        User newTrainerUser = new User("Mike", "Brown", "Mike.Brown", "x", true, 20L);
        Trainer newTrainer = new Trainer(2L, null, 20L);

        when(userDao.findAll()).thenReturn(List.of(traineeUser, oldTrainerUser, newTrainerUser));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerDao.findById(5L)).thenReturn(Optional.of(oldTrainer));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(newTrainer));
        when(trainerDao.findAll()).thenReturn(List.of(oldTrainer, newTrainer));
        when(traineeDao.update(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(trainerDao.update(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        traineeService.updateTraineeTrainersList("Ani.Smith", List.of("Mike.Brown"));

        assertEquals(Set.of(2L), trainee.getTrainerIds());
        assertFalse(oldTrainer.getTraineeIds().contains(1L));
        assertTrue(newTrainer.getTraineeIds().contains(1L));
        verify(traineeDao).update(trainee);
    }

    @Test
    void getTrainersNotAssignedToTrainee_filtersAssignedAndInactive() {
        User traineeUser = new User("Ani", "Smith", "Ani.Smith", "pass", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        trainee.setTrainerIds(Set.of(2L));

        TrainingType cardio = new TrainingType("Cardio", 1L);
        User assignedUser = new User("Mike", "Brown", "Mike.Brown", "x", true, 20L);
        Trainer assigned = new Trainer(2L, cardio, 20L);
        assigned.setUser(assignedUser);

        User freeUser = new User("Sara", "Lee", "Sara.Lee", "x", true, 30L);
        Trainer free = new Trainer(3L, cardio, 30L);
        free.setUser(freeUser);

        User inactiveUser = new User("Tom", "Idle", "Tom.Idle", "x", false, 40L);
        Trainer inactive = new Trainer(4L, cardio, 40L);
        inactive.setUser(inactiveUser);

        when(userDao.findAll()).thenReturn(List.of(traineeUser));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));
        when(trainerDao.findAll()).thenReturn(List.of(assigned, free, inactive));
        when(userDao.findById(20L)).thenReturn(Optional.of(assignedUser));
        when(userDao.findById(30L)).thenReturn(Optional.of(freeUser));
        when(userDao.findById(40L)).thenReturn(Optional.of(inactiveUser));

        List<Trainer> result = traineeService.getTrainersNotAssignedToTrainee("Ani.Smith");

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
    }

    @Test
    void setTraineeActive_togglesFromInactiveToActive() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", false, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));
        when(userDao.findById(10L)).thenReturn(Optional.of(user));

        Trainee result = traineeService.setTraineeActive(1L, true);

        assertTrue(user.isActive());
        assertSame(trainee, result);
        verify(userDao).update(user);
    }
}
