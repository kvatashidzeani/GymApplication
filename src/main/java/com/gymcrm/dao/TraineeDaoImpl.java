package com.gymcrm.dao;

import com.gymcrm.model.Trainee;
import com.gymcrm.storage.InMemoryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private static final Logger logger = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private InMemoryStorage storage;

    @Autowired
    public void setStorage(InMemoryStorage storage) {
        this.storage = storage;
    }

    private Map<Long, Trainee> store() {
        return storage.getTraineeStorage();
    }

    @Override
    public Trainee save(Trainee trainee) {
        logger.debug("Saving trainee with userId: {}", trainee.getUserId());
        store().put(trainee.getUserId(), trainee);
        logger.info("Trainee saved: {}", trainee.getUsername());
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        logger.debug("Finding trainee by id: {}", id);
        return Optional.ofNullable(store().get(id));
    }

    @Override
    public List<Trainee> findAll() {
        logger.debug("Retrieving all trainees, count: {}", store().size());
        return List.copyOf(store().values());
    }

    @Override
    public Trainee update(Trainee trainee) {
        logger.debug("Updating trainee with userId: {}", trainee.getUserId());
        if (!store().containsKey(trainee.getUserId())) {
            logger.warn("Trainee not found for update, userId: {}", trainee.getUserId());
            throw new IllegalArgumentException("Trainee not found with id: " + trainee.getUserId());
        }
        store().put(trainee.getUserId(), trainee);
        logger.info("Trainee updated: {}", trainee.getUsername());
        return trainee;
    }

    @Override
    public void deleteById(Long id) {
        logger.debug("Deleting trainee with id: {}", id);
        if (!store().containsKey(id)) {
            logger.warn("Trainee not found for deletion, id: {}", id);
            throw new IllegalArgumentException("Trainee not found with id: " + id);
        }
        store().remove(id);
        logger.info("Trainee deleted, id: {}", id);
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