package com.gymcrm.dao;

import com.gymcrm.model.Trainer;
import com.gymcrm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    private Map<Long, Trainer> store() {
        return storage.getTrainerStorage();
    }

    @Override
    public Trainer save(Trainer trainer) {
        logger.debug("Saving trainer with userId: {}", trainer.getUserId());
        store().put(trainer.getUserId(), trainer);
        logger.info("Trainer saved: {}", trainer.getUsername());
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        logger.debug("Finding trainer by id: {}", id);
        return Optional.ofNullable(store().get(id));
    }

    @Override
    public List<Trainer> findAll() {
        logger.debug("Retrieving all trainers, count: {}", store().size());
        return List.copyOf(store().values());
    }

    @Override
    public Trainer update(Trainer trainer) {
        logger.debug("Updating trainer with userId: {}", trainer.getUserId());
        if (!store().containsKey(trainer.getUserId())) {
            logger.warn("Trainer not found for update, userId: {}", trainer.getUserId());
            throw new IllegalArgumentException("Trainer not found with id: " + trainer.getUserId());
        }
        store().put(trainer.getUserId(), trainer);
        logger.info("Trainer updated: {}", trainer.getUsername());
        return trainer;
    }

    @Override
    public boolean existsByUsername(String username) {
        return store().values().stream()
                .anyMatch(t -> t.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public long countByFirstAndLastName(String firstName, String lastName) {
        return store().values().stream()
                .filter(t -> t.getFirstName().equalsIgnoreCase(firstName)
                        && t.getLastName().equalsIgnoreCase(lastName))
                .count();
    }
}