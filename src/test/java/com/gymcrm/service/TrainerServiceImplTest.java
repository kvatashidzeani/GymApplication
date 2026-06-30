package org.example.ServiceTests;

import com.gym.crm.exceptions.TrainerNotFoundException;
import com.gym.crm.Util.IdGenerator;
import com.gym.crm.Util.PasswordGenerator;
import com.gym.crm.Util.UsernameGenerator;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainerService;
import com.gym.crm.validators.TrainerValidator;
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
        TrainingType trainingType = new TrainingType(1L, "Cardio");
        when(idGenerator.generateNextId()).thenReturn(100L);
        when(usernameGenerator.generateUsername("Ani", "Smith")).thenReturn("anismith");
        when(passwordGenerator.generatePassword()).thenReturn("pass123");
        Trainer saved = new Trainer();
        saved.setId(100L);
        saved.setUsername("anismith");
        when(trainerDao.save(any())).thenReturn(saved);

        Trainer result = trainerService.createTrainer("Ani", "Smith", trainingType);

        assertEquals(100L, result.getId());
        assertEquals("anismith", result.getUsername());
        verify(trainerDao, times(1)).save(any());
    }

    @Test
    void updateTrainer_success() {
        TrainingType trainingType = new TrainingType(2L, "Strength");
        Trainer existing = new Trainer();
        existing.setId(50L);
        existing.setUsername("trainer50");
        when(trainerDao.findById(50L)).thenReturn(Optional.of(existing));
        when(trainerDao.update(existing)).thenReturn(existing);

        Trainer updated = trainerService.updateTrainer(50L, "Ani", "Smith", trainingType, true);

        assertEquals(50L, updated.getId());
        assertEquals("trainer50", updated.getUsername());
        assertTrue(updated.isActive());
        assertEquals(trainingType, updated.getSpecialization());
    }

    @Test
    void updateTrainer_notFound() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());
        TrainingType type = new TrainingType(1L, "Yoga");

        assertThrows(TrainerNotFoundException.class,
                () -> trainerService.updateTrainer(99L, "A", "B", type, true));
    }

    @Test
    void selectTrainer_success() {
        Trainer t = new Trainer();
        t.setId(10L);
        t.setUsername("trainer10");
        t.setSpecialization(new TrainingType(1L, "Pilates"));
        when(trainerDao.findById(10L)).thenReturn(Optional.of(t));

        Trainer result = trainerService.selectTrainer(10L);
        assertEquals(10L, result.getId());
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
        t1.setId(1L);
        Trainer t2 = new Trainer();
        t2.setId(2L);
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
                trainerService.updateTrainer(null, "Ani", "Smith", new TrainingType(1L, "Cardio"), true));
    }

    @Test
    void selectTrainerByUsername_notFound() {
        when(trainerDao.findAll()).thenReturn(List.of());
        assertThrows(TrainerNotFoundException.class,
                () -> trainerService.selectTrainerByUsername("nonexistent"));
    }
}