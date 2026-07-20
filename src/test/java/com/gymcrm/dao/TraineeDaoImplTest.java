package com.gymcrm.dao;

import com.gymcrm.exceptions.TraineeNotFoundException;
import com.gymcrm.dao.impl.TraineeDaoImpl;
import com.gymcrm.model.Trainee;
import com.gymcrm.storage.TraineeStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeDaoImplTest {

    private TraineeStorage traineeStorage;
    private TraineeDaoImpl traineeDao;
    private Map<Long, Trainee> storageMap;

    @BeforeEach
    void setUp() {
        traineeStorage = mock(TraineeStorage.class);
        storageMap = new HashMap<>();
        when(traineeStorage.getStorage()).thenReturn(storageMap);
        traineeDao = new TraineeDaoImpl();
        traineeDao.setTraineeStorage(traineeStorage);
    }

    @Test
    void saveTrainee() {
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        Trainee saved = traineeDao.save(trainee);
        assertEquals(trainee, saved);
        assertTrue(storageMap.containsKey(1L));
    }

    @Test
    void saveTraineeThrowsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.save(null));
        assertThrows(IllegalArgumentException.class, () -> traineeDao.save(new Trainee()));
    }

    @Test
    void updateTraineeSuccess() {
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        storageMap.put(1L, trainee);
        assertEquals(trainee, traineeDao.update(trainee));
    }

    @Test
    void updateTraineeNotFound() {
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        assertThrows(TraineeNotFoundException.class, () -> traineeDao.update(trainee));
    }

    @Test
    void findByIdFound() {
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        storageMap.put(1L, trainee);
        Optional<Trainee> result = traineeDao.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
    }

    @Test
    void findAllReturnsAllTrainees() {
        storageMap.put(1L, new Trainee(1L, LocalDate.of(2000, 1, 1), "A", 10L));
        storageMap.put(2L, new Trainee(2L, LocalDate.of(2001, 1, 1), "B", 11L));
        List<Trainee> all = traineeDao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void deleteRemovesTrainee() {
        storageMap.put(1L, new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L));
        traineeDao.delete(1L);
        assertFalse(storageMap.containsKey(1L));
    }
}
