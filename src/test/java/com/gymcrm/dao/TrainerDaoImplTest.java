package com.gymcrm.dao;

import com.gymcrm.model.Trainer;
import com.gymcrm.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoImplTest {

    private TrainerDaoImpl trainerDao;
    private InMemoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
        trainerDao = new TrainerDaoImpl();
        trainerDao.setStorage(storage);
    }

    @Test
    void save_andFindById_returnsTrainer() {
        Trainer trainer = new Trainer("Mike", "Brown", "Mike.Brown", "pass",
                true, 1L, "Cardio");

        trainerDao.save(trainer);
        Optional<Trainer> found = trainerDao.findById(1L);

        assertTrue(found.isPresent());
        assertEquals("Cardio", found.get().getSpecialization());
    }

    @Test
    void update_existingTrainer_updatesFields() {
        Trainer trainer = new Trainer("Mike", "Brown", "Mike.Brown", "pass",
                true, 1L, "Cardio");
        trainerDao.save(trainer);

        trainer.setSpecialization("Strength");
        Trainer updated = trainerDao.update(trainer);

        assertEquals("Strength", updated.getSpecialization());
    }

    @Test
    void update_notFound_throwsException() {
        Trainer trainer = new Trainer("Ghost", "Trainer", "Ghost.Trainer", "pass",
                true, 99L, "Yoga");

        assertThrows(IllegalArgumentException.class, () -> trainerDao.update(trainer));
    }

    @Test
    void existsByUsername_returnsTrueWhenPresent() {
        trainerDao.save(new Trainer("Jane", "Doe", "Jane.Doe", "pass", true, 1L, "Yoga"));

        assertTrue(trainerDao.existsByUsername("Jane.Doe"));
        assertFalse(trainerDao.existsByUsername("Unknown.User"));
    }
}
