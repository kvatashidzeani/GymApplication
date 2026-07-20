package com.gymcrm.dao;

import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.dao.impl.TrainerDaoImpl;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.storage.TrainerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerDaoImplTest {

    private TrainerStorage trainerStorage;
    private TrainerDaoImpl trainerDao;
    private Map<Long, Trainer> storageMap;

    @BeforeEach
    void setUp() {
        trainerStorage = mock(TrainerStorage.class);
        storageMap = new HashMap<>();
        when(trainerStorage.getStorage()).thenReturn(storageMap);
        trainerDao = new TrainerDaoImpl();
        trainerDao.setTrainerStorage(trainerStorage);
    }

    @Test
    void saveTrainer() {
        Trainer trainer = new Trainer(1L, new TrainingType("Cardio", 1L), 10L);
        assertEquals(trainer, trainerDao.save(trainer));
        assertTrue(storageMap.containsKey(1L));
    }

    @Test
    void saveTrainerThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.save(null));
    }

    @Test
    void updateTrainerSuccess() {
        Trainer trainer = new Trainer(1L, new TrainingType("Cardio", 1L), 10L);
        storageMap.put(1L, trainer);
        assertEquals(trainer, trainerDao.update(trainer));
    }

    @Test
    void updateTrainerNotFound() {
        Trainer trainer = new Trainer(1L, new TrainingType("Cardio", 1L), 10L);
        assertThrows(TrainerNotFoundException.class, () -> trainerDao.update(trainer));
    }

    @Test
    void findByIdFound() {
        Trainer trainer = new Trainer(1L, new TrainingType("Cardio", 1L), 10L);
        storageMap.put(1L, trainer);
        Optional<Trainer> result = trainerDao.findById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void findAllReturnsAllTrainers() {
        storageMap.put(1L, new Trainer(1L, new TrainingType("Cardio", 1L), 10L));
        storageMap.put(2L, new Trainer(2L, new TrainingType("Yoga", 2L), 11L));
        assertEquals(2, trainerDao.findAll().size());
    }

    @Test
    void deleteRemovesTrainer() {
        storageMap.put(1L, new Trainer(1L, new TrainingType("Cardio", 1L), 10L));
        trainerDao.delete(1L);
        assertFalse(storageMap.containsKey(1L));
    }
}
