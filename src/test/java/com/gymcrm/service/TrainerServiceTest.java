package com.gymcrm.service;

import com.gymcrm.Util.IdGenerator;
import com.gymcrm.Util.PasswordGenerator;
import com.gymcrm.Util.UsernameGenerator;
import com.gymcrm.dao.TraineeDao;
import com.gymcrm.dao.TrainerDao;
import com.gymcrm.dao.TrainingDao;
import com.gymcrm.dao.UserDao;
import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.validators.TrainerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerService trainerService;
    private TrainerDao trainerDao;
    private TraineeDao traineeDao;
    private UserDao userDao;
    private IdGenerator idGenerator;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TrainerValidator trainerValidator;

    @BeforeEach
    void setUp() {
        trainerDao = mock(TrainerDao.class);
        traineeDao = mock(TraineeDao.class);
        userDao = mock(UserDao.class);
        idGenerator = mock(IdGenerator.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        trainerValidator = mock(TrainerValidator.class);

        trainerService = new TrainerService();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setUserDao(userDao);
        trainerService.setTraineeDao(traineeDao);
        trainerService.setTrainingDao(mock(TrainingDao.class));
        trainerService.setIdGenerator(idGenerator);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
        trainerService.setTrainerValidator(trainerValidator);
    }

    @Test
    void createTrainer_savesUserAndTrainer() {
        TrainingType cardio = new TrainingType("Cardio", 1L);
        when(idGenerator.generateNextId()).thenReturn(10L, 20L);
        when(usernameGenerator.generateUsername("Giorgi", "Janelidze")).thenReturn("Giorgi.Janelidze");
        when(passwordGenerator.generatePassword()).thenReturn("secret");
        when(traineeDao.findAll()).thenReturn(List.of());
        when(userDao.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(trainerDao.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer result = trainerService.createTrainer("Giorgi", "Janelidze", cardio);

        assertEquals(20L, result.getId());
        assertEquals(10L, result.getUserId());
        assertEquals(cardio, result.getSpecialization());
        verify(userDao).save(any(User.class));
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void createTrainer_rejectsIfAlreadyTrainee() {
        TrainingType cardio = new TrainingType("Cardio", 1L);
        User traineeUser = new User("Giorgi", "Janelidze", "Giorgi.Janelidze", "x", true, 5L);
        Trainee trainee = new Trainee(10L, null, null, 5L);
        when(traineeDao.findAll()).thenReturn(List.of(trainee));
        when(userDao.findById(5L)).thenReturn(Optional.of(traineeUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> trainerService.createTrainer("Giorgi", "Janelidze", cardio));

        assertTrue(ex.getMessage().contains("already registered as trainee"));
        verify(trainerDao, never()).save(any());
    }

    @Test
    void updateTrainer_updatesUserAndTrainer() {
        TrainingType yoga = new TrainingType("Yoga", 2L);
        User user = new User("Giorgi", "Janelidze", "Giorgi.Janelidze", "pass", true, 10L);
        Trainer trainer = new Trainer(1L, new TrainingType("Cardio", 1L), 10L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(userDao.findById(10L)).thenReturn(Optional.of(user));
        when(trainerDao.update(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer updated = trainerService.updateTrainer(1L, "Giorgi", "Janelidze", yoga, false);

        assertEquals(yoga, updated.getSpecialization());
        assertFalse(user.isActive());
        verify(userDao).update(user);
    }

    @Test
    void updateTrainer_notFound_throws() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());
        TrainingType type = new TrainingType("Yoga", 1L);

        assertThrows(TrainerNotFoundException.class,
                () -> trainerService.updateTrainer(99L, "A", "B", type, true));
    }

    @Test
    void setTrainerActive_whenAlreadyInactive_throws() {
        User user = new User("Giorgi", "Janelidze", "Giorgi.Janelidze", "pass", false, 10L);
        Trainer trainer = new Trainer(1L, new TrainingType("Cardio", 1L), 10L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));
        when(userDao.findById(10L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> trainerService.setTrainerActive(1L, false));
    }

    @Test
    void selectTrainerByUsername_success() {
        User user = new User("Giorgi", "Janelidze", "Giorgi.Janelidze", "pass", true, 10L);
        Trainer trainer = new Trainer(1L, new TrainingType("Cardio", 1L), 10L);
        when(userDao.findAll()).thenReturn(List.of(user));
        when(trainerDao.findAll()).thenReturn(List.of(trainer));

        Trainer result = trainerService.selectTrainerByUsername("Giorgi.Janelidze");

        assertEquals(1L, result.getId());
        assertEquals("Giorgi.Janelidze", result.getUser().getUsername());
    }

    @Test
    void selectTrainer_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.selectTrainer(null));
    }
}
