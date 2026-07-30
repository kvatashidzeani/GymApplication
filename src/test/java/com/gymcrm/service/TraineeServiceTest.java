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
import com.gymcrm.model.User;
import com.gymcrm.validators.TraineeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setTrainerDao(trainerDao);
        traineeService.setTrainingDao(trainingDao);
        traineeService.setUserDao(userDao);
        traineeService.setIdGenerator(idGenerator);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
        traineeService.setTraineeValidator(traineeValidator);
    }

    @Test
    void createTrainee_savesUserAndTrainee() {
        when(idGenerator.generateNextId()).thenReturn(1L, 2L);
        when(usernameGenerator.generateUsername("Ani", "Smith")).thenReturn("Ani.Smith");
        when(passwordGenerator.generatePassword()).thenReturn("secret");
        when(trainerDao.findAll()).thenReturn(List.of());
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(traineeDao.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee result = traineeService.createTrainee(
                "Ani", "Smith", LocalDate.of(2000, 1, 1), "Tbilisi");

        assertEquals(2L, result.getId());
        assertEquals(1L, result.getUserId());
        assertNotNull(result.getUser());
        assertEquals("Ani.Smith", result.getUser().getUsername());
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
        User user = new User("Ani", "Smith", "Ani.Smith", "secret", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));

        assertTrue(traineeService.matchTraineeCredentials("Ani.Smith", "secret"));
    }

    @Test
    void matchTraineeCredentials_wrongPassword_returnsFalse() {
        User user = new User("Ani", "Smith", "Ani.Smith", "secret", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));

        assertFalse(traineeService.matchTraineeCredentials("Ani.Smith", "wrong"));
    }

    @Test
    void changeTraineePassword_success() {
        User user = new User("Ani", "Smith", "Ani.Smith", "old", true, 10L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(traineeDao.findAll()).thenReturn(List.of(trainee));

        traineeService.changeTraineePassword("Ani.Smith", "old", "new");

        assertEquals("new", user.getPassword());
        verify(userDao).update(user);
    }

    @Test
    void select_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.select(null));
    }
}
