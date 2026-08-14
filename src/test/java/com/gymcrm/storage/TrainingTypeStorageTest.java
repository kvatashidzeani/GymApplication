package com.gymcrm.storage;

import com.gymcrm.Util.IdGenerator;
import com.gymcrm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingTypeStorageTest {

    private TrainingTypeStorage storage;
    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        storage = new TrainingTypeStorage();
        idGenerator = mock(IdGenerator.class);
        when(idGenerator.generateNextId()).thenReturn(1L, 2L);
        storage.setIdGenerator(idGenerator);
    }

    @Test
    void seedTrainingType_storesAndFindsById() {
        TrainingType cardio = storage.seedTrainingType("Cardio");
        assertEquals("Cardio", cardio.getTrainingTypeName());
        assertEquals(1L, cardio.getTrainingTypeId());
        assertSame(cardio, storage.get(1L));
    }

    @Test
    void findAll_returnsSeededTypes() {
        storage.seedTrainingType("Cardio");
        storage.seedTrainingType("Strength");
        assertEquals(2, storage.findAll().size());
    }

    @Test
    void requireByName_returnsType() {
        storage.seedTrainingType("Yoga");
        TrainingType type = storage.requireByName("Yoga");
        assertEquals("Yoga", type.getTrainingTypeName());
    }

    @Test
    void requireByName_unknown_throws() {
        assertThrows(IllegalArgumentException.class, () -> storage.requireByName("Unknown"));
    }
}
