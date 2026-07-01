package com.gymcrm.service;

import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.Util.IdGenerator;
import com.gymcrm.Util.PasswordGenerator;
import com.gymcrm.Util.UsernameGenerator;
import com.gymcrm.dao.TrainerDao;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.TrainerService;
import com.gymcrm.validators.TrainerValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private IdGenerator idGenerator;
    private TrainerService trainerService;
    private TrainerValidator trainerValidator;
    @BeforeEach
    void setup() {
        trainerDao = mock(TrainerDao.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        idGenerator = mock(IdGenerator.class);
        trainerValidator = mock(TrainerValidator.class);

        trainerService = new TrainerService();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
        trainerService.setIdGenerator(idGenerator);
        trainerService.setTrainerValidator(trainerValidator);
    }

    @Test
    void createTrainer_success() {
        TrainingType trainingType = new TrainingType("Cardio", 1L);
        when(idGenerator.generateNextId()).thenReturn(100L);
        when(usernameGenerator.generateUsername("John", "Doe")).thenReturn("johndoe");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");
        Trainer saved = new Trainer();
        saved.setTrainerId(100L);
        saved.setUsername("johndoe");
        when(trainerDao.save(any())).thenReturn(saved);

        Trainer result = trainerService.createTrainer("John", "Doe", trainingType);

        assertEquals(100L, result.getTrainerId());
        assertEquals("johndoe", result.getUsername());
        verify(trainerDao, times(1)).save(any());
    }

    @Test
    void updateTrainer_success() {
        TrainingType trainingType = new TrainingType("Strength",2L);
        Trainer existing = new Trainer();
        existing.setTrainerId(50L);
        existing.setUsername("trainer50");
        when(trainerDao.findById(50L)).thenReturn(Optional.of(existing));
        when(trainerDao.update(existing)).thenReturn(existing);

        Trainer updated = trainerService.updateTrainer(50L, "Jane", "Smith", trainingType, true);

        assertEquals(50L, updated.getTrainerId());
        assertEquals("trainer50", updated.getUsername());
        assertTrue(updated.isActive());
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
        t.setTrainerId(10L);
        t.setUsername("trainer10");
        t.setSpecialization(new TrainingType("Pilates", 1L));
        when(trainerDao.findById(10L)).thenReturn(Optional.of(t));

        Trainer result = trainerService.selectTrainer(10L);
        assertEquals(10L, result.getTrainerId());
        assertEquals("trainer10", result.getUsername());
    }

    @Test
    void selectTrainer_notFound() {
        when(trainerDao.findById(999L)).thenReturn(Optional.empty());
        assertThrows(TrainerNotFoundException.class, () -> trainerService.selectTrainer(999L));
    }

    @Test
    void selectAllTrainers_success() {
        Trainer t1 = new Trainer();
        t1.setTrainerId(1L);
        Trainer t2 = new Trainer();
        t2.setTrainerId(2L);
        when(trainerDao.findAll()).thenReturn(List.of(t1, t2));

        List<Trainer> result = trainerService.selectAllTrainers();
        assertEquals(2, result.size());
    }

    @Test
    void selectTrainerByUsername_success() {
        Trainer t = new Trainer();
        t.setUsername("uniqueUser");
        when(trainerDao.findAll()).thenReturn(List.of(t));

        Trainer result = trainerService.selectTrainerByUsername("uniqueUser");
        assertEquals("uniqueUser", result.getUsername());
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
        when(trainerDao.findAll()).thenReturn(List.of());
        assertThrows(TrainerNotFoundException.class,
                () -> trainerService.selectTrainerByUsername("nonexistent"));
    }
}
