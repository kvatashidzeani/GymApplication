package com.gymcrm.dao;

import com.gymcrm.dao.impl.TrainingDaoImpl;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.storage.TrainingStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingDaoImplTest {

    private TrainingStorage trainingStorage;
    private TrainingDaoImpl trainingDao;
    private Map<Long, Training> storageMap;

    @BeforeEach
    void setUp() {
        trainingStorage = mock(TrainingStorage.class);
        storageMap = new HashMap<>();
        when(trainingStorage.getStorage()).thenReturn(storageMap);
        trainingDao = new TrainingDaoImpl();
        trainingDao.setTrainingStorage(trainingStorage);
    }

    @Test
    void saveTraining() {
        Training training = new Training(1L, 10L, 20L, "Cardio",
                new TrainingType("Cardio", 1L), LocalDate.of(2024, 1, 1), 60);
        assertEquals(training, trainingDao.save(training));
        assertTrue(storageMap.containsKey(1L));
    }

    @Test
    void saveTrainingThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> trainingDao.save(null));
        Training training = new Training();
        assertThrows(IllegalArgumentException.class, () -> trainingDao.save(training));
    }

    @Test
    void findByIdFound() {
        Training training = new Training(1L, 10L, 20L, "Cardio",
                new TrainingType("Cardio", 1L), LocalDate.of(2024, 1, 1), 60);
        storageMap.put(1L, training);
        Optional<Training> result = trainingDao.findById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void findByTrainerId() {
        Training t1 = new Training(1L, 10L, 100L, "A", new TrainingType("Cardio", 1L), LocalDate.now(), 30);
        Training t2 = new Training(2L, 11L, 101L, "B", new TrainingType("Yoga", 2L), LocalDate.now(), 30);
        storageMap.put(1L, t1);
        storageMap.put(2L, t2);
        List<Training> result = trainingDao.findByTrainerId(100L);
        assertEquals(1, result.size());
        assertEquals(t1, result.get(0));
    }

    @Test
    void findByTraineeId() {
        Training t1 = new Training(1L, 200L, 100L, "A", new TrainingType("Cardio", 1L), LocalDate.now(), 30);
        Training t2 = new Training(2L, 201L, 101L, "B", new TrainingType("Yoga", 2L), LocalDate.now(), 30);
        storageMap.put(1L, t1);
        storageMap.put(2L, t2);
        List<Training> result = trainingDao.findByTraineeId(200L);
        assertEquals(1, result.size());
        assertEquals(t1, result.get(0));
    }

    @Test
    void deleteRemovesTraining() {
        storageMap.put(1L, new Training(1L, 10L, 20L, "Cardio",
                new TrainingType("Cardio", 1L), LocalDate.of(2024, 1, 1), 60));
        trainingDao.delete(1L);
        assertFalse(storageMap.containsKey(1L));
    }
}
