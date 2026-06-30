package org.example.DaoTests;


import com.gym.crm.dao.impl.TrainingDaoImpl;
import com.gym.crm.model.Training;
import com.gym.crm.storage.TrainingStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingDaoImplTest {

    private TrainingStorage trainingStorage;
    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void setUp() {
        trainingStorage = mock(TrainingStorage.class);
        trainingDao = new TrainingDaoImpl();
        trainingDao.setTrainingStorage(trainingStorage);
    }

    @Test
    void testSaveTraining() {
        Training training = new Training();
        training.setId(1L);

        Map<Long, Training> storageMap = new HashMap<>();
        when(trainingStorage.getStorage()).thenReturn(storageMap);

        Training saved = trainingDao.save(training);

        assertEquals(training, saved);
        assertTrue(storageMap.containsKey(1L));
        assertEquals(training, storageMap.get(1L));
    }

    @Test
    void testSaveTrainingThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.save(null));

        Training training = new Training();
        training.setId(null);
        assertThrows(IllegalArgumentException.class, () -> trainingDao.save(training));
    }

    @Test
    void testFindByIdFound() {
        Training training = new Training();
        training.setId(1L);

        Map<Long, Training> storageMap = new HashMap<>();
        storageMap.put(1L, training);
        when(trainingStorage.getStorage()).thenReturn(storageMap);

        Optional<Training> result = trainingDao.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(training, result.get());
    }

    @Test
    void testFindByIdNotFound() {
        Map<Long, Training> storageMap = new HashMap<>();
        when(trainingStorage.getStorage()).thenReturn(storageMap);

        Optional<Training> result = trainingDao.findById(1L);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        Training t1 = new Training();
        t1.setId(1L);
        Training t2 = new Training();
        t2.setId(2L);

        Map<Long, Training> storageMap = new HashMap<>();
        storageMap.put(1L, t1);
        storageMap.put(2L, t2);
        when(trainingStorage.getStorage()).thenReturn(storageMap);

        List<Training> all = trainingDao.findAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(t1));
        assertTrue(all.contains(t2));
    }

    @Test
    void testDelete() {
        Training t1 = new Training();
        t1.setId(1L);

        Map<Long, Training> storageMap = new HashMap<>();
        storageMap.put(1L, t1);
        when(trainingStorage.getStorage()).thenReturn(storageMap);

        trainingDao.delete(1L);
        assertFalse(storageMap.containsKey(1L));
    }

    @Test
    void testFindByTrainerId() {
        Training t1 = new Training(); t1.setId(1L); t1.setTrainerId(100L);
        Training t2 = new Training(); t2.setId(2L); t2.setTrainerId(101L);

        Map<Long, Training> storageMap = new HashMap<>();
        storageMap.put(1L, t1);
        storageMap.put(2L, t2);
        when(trainingStorage.getStorage()).thenReturn(storageMap);

        List<Training> result = trainingDao.findByTrainerId(100L);
        assertEquals(1, result.size());
        assertEquals(t1, result.get(0));
    }

    @Test
    void testFindByTraineeId() {
        Training t1 = new Training(); t1.setId(1L); t1.setTraineeId(200L);
        Training t2 = new Training(); t2.setId(2L); t2.setTraineeId(201L);

        Map<Long, Training> storageMap = new HashMap<>();
        storageMap.put(1L, t1);
        storageMap.put(2L, t2);
        when(trainingStorage.getStorage()).thenReturn(storageMap);

        List<Training> result = trainingDao.findByTraineeId(200L);
        assertEquals(1, result.size());
        assertEquals(t1, result.get(0));
    }
}