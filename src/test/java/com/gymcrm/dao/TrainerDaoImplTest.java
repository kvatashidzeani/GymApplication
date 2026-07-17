package com.gymcrm.dao;

import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.dao.impl.TrainerDaoImpl;
import com.gymcrm.model.Trainer;
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

    @BeforeEach
    void setUp() {
        trainerStorage = mock(TrainerStorage.class);
        trainerDao = new TrainerDaoImpl();
        trainerDao.setTrainerStorage(trainerStorage);
    }

    @Test
    void testSaveTrainer() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUserId(10L);

        Map<Long, Trainer> storageMap = new HashMap<>();
        when(trainerStorage.getStorage()).thenReturn(storageMap);

        Trainer saved = trainerDao.save(trainer);

        assertEquals(trainer, saved);
        assertTrue(storageMap.containsKey(1L));
    }

    @Test
    void testSaveTrainerThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.save(null));
    }

    @Test
    void testUpdateTrainerSuccess() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        Map<Long, Trainer> storageMap = new HashMap<>();
        storageMap.put(1L, trainer);
        when(trainerStorage.getStorage()).thenReturn(storageMap);

        Trainer updated = trainerDao.update(trainer);

        assertEquals(trainer, updated);
        assertEquals(trainer, storageMap.get(1L));
    }

    @Test
    void testUpdateTrainerNotFound() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        Map<Long, Trainer> storageMap = new HashMap<>();
        when(trainerStorage.getStorage()).thenReturn(storageMap);

        assertThrows(TrainerNotFoundException.class, () -> trainerDao.update(trainer));
    }

    @Test
    void testFindByIdFound() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        Map<Long, Trainer> storageMap = new HashMap<>();
        storageMap.put(1L, trainer);
        when(trainerStorage.getStorage()).thenReturn(storageMap);

        Optional<Trainer> result = trainerDao.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
    }

    @Test
    void testFindByIdNotFound() {
        Map<Long, Trainer> storageMap = new HashMap<>();
        when(trainerStorage.getStorage()).thenReturn(storageMap);

        Optional<Trainer> result = trainerDao.findById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        Trainer t1 = new Trainer();
        t1.setId(1L);
        Trainer t2 = new Trainer();
        t2.setId(2L);

        Map<Long, Trainer> storageMap = new HashMap<>();
        storageMap.put(1L, t1);
        storageMap.put(2L, t2);
        when(trainerStorage.getStorage()).thenReturn(storageMap);

        List<Trainer> all = trainerDao.findAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(t1));
        assertTrue(all.contains(t2));
    }

    @Test
    void testDelete() {
        Trainer t1 = new Trainer();
        t1.setId(1L);

        Map<Long, Trainer> storageMap = new HashMap<>();
        storageMap.put(1L, t1);
        when(trainerStorage.getStorage()).thenReturn(storageMap);

        trainerDao.delete(1L);
        assertFalse(storageMap.containsKey(1L));
    }
}
