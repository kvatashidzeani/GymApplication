package com.gymcrm.service;

import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.Util.IdGenerator;
import com.gymcrm.Util.PasswordGenerator;
import com.gymcrm.Util.UsernameGenerator;
import com.gymcrm.dao.TrainerDao;
import com.gymcrm.dao.UserDao;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.validators.TrainerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerDao trainerDao;
    private UserDao userDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private IdGenerator idGenerator;
    private TrainerService trainerService;
    private TrainerValidator trainerValidator;

    @BeforeEach
    void setup() {
        trainerDao = mock(TrainerDao.class);
        userDao = mock(UserDao.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        idGenerator = mock(IdGenerator.class);
        trainerValidator = mock(TrainerValidator.class);

        trainerService = new TrainerService();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setUserDao(userDao);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
        trainerService.setIdGenerator(idGenerator);
        trainerService.setTrainerValidator(trainerValidator);
    }

    @Test
    void createTrainer_success() {
        TrainingType trainingType = new TrainingType("Cardio", 1L);
        when(idGenerator.generateNextId()).thenReturn(100L, 101L);
        when(usernameGenerator.generateUsername("John", "Doe")).thenReturn("johndoe");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(trainerDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.createTrainer("John", "Doe", trainingType);

        assertEquals(101L, result.getId());
        assertEquals(100L, result.getUserId());
        assertEquals("johndoe", result.getUser().getUsername());
        verify(userDao, times(1)).save(any(User.class));
        verify(trainerDao, times(1)).save(any());
    }

    @Test
    void updateTrainer_success() {
        TrainingType trainingType = new TrainingType("Strength", 2L);
        Trainer existing = new Trainer();
        existing.setId(50L);
        existing.setUserId(5L);
        User user = new User("Old", "Name", "trainer50", "p", true, 5L);
        when(trainerDao.findById(50L)).thenReturn(Optional.of(existing));
        when(userDao.findById(5L)).thenReturn(Optional.of(user));
        when(userDao.update(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(trainerDao.update(existing)).thenReturn(existing);

        Trainer updated = trainerService.updateTrainer(50L, "Jane", "Smith", trainingType, true);

        assertEquals(50L, updated.getId());
        assertEquals("trainer50", updated.getUser().getUsername());
        assertTrue(updated.getUser().isActive());
        assertEquals(trainingType, updated.getSpecialization());
    }

    @Test
    void updateTrainer_notFound() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());
        TrainingType type = new TrainingType("Yoga", 1L);

        assertThrows(TrainerNotFoundException.class,
                () -> trainerService.updateTrainer(99L, "A", "B", type, true));
    }

    @Test
    void selectTrainer_success() {
        Trainer t = new Trainer();
        t.setId(10L);
        t.setUserId(20L);
        t.setSpecialization(new TrainingType("Pilates", 1L));
        User user = new User("T", "R", "trainer10", "p", true, 20L);
        when(trainerDao.findById(10L)).thenReturn(Optional.of(t));
        when(userDao.findById(20L)).thenReturn(Optional.of(user));

        Trainer result = trainerService.selectTrainer(10L);
        assertEquals(10L, result.getId());
        assertEquals("trainer10", result.getUser().getUsername());
    }

    @Test
    void selectTrainer_notFound() {
        when(trainerDao.findById(999L)).thenReturn(Optional.empty());
        assertThrows(TrainerNotFoundException.class, () -> trainerService.selectTrainer(999L));
    }

    @Test
    void selectAllTrainers_success() {
        Trainer t1 = new Trainer();
        t1.setId(1L);
        Trainer t2 = new Trainer();
        t2.setId(2L);
        when(trainerDao.findAll()).thenReturn(List.of(t1, t2));

        List<Trainer> result = trainerService.selectAllTrainers();
        assertEquals(2, result.size());
    }

    @Test
    void selectTrainerByUsername_success() {
        User user = new User("U", "N", "uniqueUser", "p", true, 7L);
        Trainer t = new Trainer();
        t.setId(1L);
        t.setUserId(7L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(trainerDao.findAll()).thenReturn(List.of(t));

        Trainer result = trainerService.selectTrainerByUsername("uniqueUser");
        assertEquals("uniqueUser", result.getUser().getUsername());
    }

    @Test
    void selectTrainer_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.selectTrainer(null));
    }

    @Test
    void updateTrainer_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                trainerService.updateTrainer(null, "John", "Doe", new TrainingType("Cardio", 1L), true));
    }

    @Test
    void selectTrainerByUsername_notFound() {
        when(userDao.findAll()).thenReturn(List.of());
        assertThrows(TrainerNotFoundException.class,
                () -> trainerService.selectTrainerByUsername("nonexistent"));
    }

    @Test
    void matchTrainerCredentials_success() {
        User user = new User("John", "Doe", "johndoe", "pass123", true, 100L);
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUserId(100L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(trainerDao.findAll()).thenReturn(List.of(trainer));

        assertTrue(trainerService.matchTrainerCredentials("johndoe", "pass123"));
    }

    @Test
    void matchTrainerCredentials_wrongPassword() {
        User user = new User("John", "Doe", "johndoe", "pass123", true, 100L);
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUserId(100L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(trainerDao.findAll()).thenReturn(List.of(trainer));

        assertFalse(trainerService.matchTrainerCredentials("johndoe", "wrong"));
    }
}
