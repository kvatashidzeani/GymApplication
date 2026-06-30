package org.example.DaoTests;


import com.gym.crm.exceptions.TraineeNotFoundException;
import com.gym.crm.dao.impl.TraineeDaoImpl;
import com.gym.crm.model.Trainee;
import com.gym.crm.storage.TraineeStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeDaoImplTest {

    private TraineeStorage traineeStorage;
    private TraineeDaoImpl traineeDao;

    @BeforeEach
    void setUp() {
        traineeStorage = mock(TraineeStorage.class);
        traineeDao = new TraineeDaoImpl();
        traineeDao.setTraineeStorage(traineeStorage);
    }

    @Test
    void testSaveTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        Map<Long, Trainee> storageMap = new HashMap<>();
        when(traineeStorage.getStorage()).thenReturn(storageMap);

        Trainee saved = traineeDao.save(trainee);

        assertEquals(trainee, saved);
        assertTrue(storageMap.containsKey(1L));
    }

    @Test
    void testSaveTraineeThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.save(null));

        Trainee t = new Trainee();
        t.setId(null);
        assertThrows(IllegalArgumentException.class, () -> traineeDao.save(t));
    }

    @Test
    void testUpdateTraineeSuccess() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        Map<Long, Trainee> storageMap = new HashMap<>();
        storageMap.put(1L, trainee);
        when(traineeStorage.getStorage()).thenReturn(storageMap);

        Trainee updated = traineeDao.update(trainee);
        assertEquals(trainee, updated);
    }

    @Test
    void testUpdateTraineeNotFound() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        Map<Long, Trainee> storageMap = new HashMap<>();
        when(traineeStorage.getStorage()).thenReturn(storageMap);

        assertThrows(TraineeNotFoundException.class, () -> traineeDao.update(trainee));
    }

    @Test
    void testFindByIdFound() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        Map<Long, Trainee> storageMap = new HashMap<>();
        storageMap.put(1L, trainee);
        when(traineeStorage.getStorage()).thenReturn(storageMap);

        Optional<Trainee> result = traineeDao.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void testFindByIdNotFound() {
        Map<Long, Trainee> storageMap = new HashMap<>();
        when(traineeStorage.getStorage()).thenReturn(storageMap);

        Optional<Trainee> result = traineeDao.findById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        Trainee t1 = new Trainee();
        t1.setId(1L);
        Trainee t2 = new Trainee();
        t2.setId(2L);

        Map<Long, Trainee> storageMap = new HashMap<>();
        storageMap.put(1L, t1);
        storageMap.put(2L, t2);
        when(traineeStorage.getStorage()).thenReturn(storageMap);

        List<Trainee> all = traineeDao.findAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(t1));
        assertTrue(all.contains(t2));
    }

    @Test
    void testDelete() {
        Trainee t1 = new Trainee();
        t1.setId(1L);

        Map<Long, Trainee> storageMap = new HashMap<>();
        storageMap.put(1L, t1);
        when(traineeStorage.getStorage()).thenReturn(storageMap);

        traineeDao.delete(1L);
        assertFalse(storageMap.containsKey(1L));
    }
}

