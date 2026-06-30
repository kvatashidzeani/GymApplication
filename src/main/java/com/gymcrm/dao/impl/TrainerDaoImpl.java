package com.gym.crm.dao.impl;

import com.gym.crm.exceptions.TrainerNotFoundException;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainer;
import com.gym.crm.storage.TrainerStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private TrainerStorage trainerStorage;
    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);



    @Autowired
    public void setTrainerStorage(TrainerStorage trainerStorage){
        this.trainerStorage = trainerStorage;
    }
    @Override
    public Trainer save(Trainer trainer) {
        log.info("Saving a trainer.");
        if (trainer == null || trainer.getId() == null) {
            log.error("The trainer or the trainer id passed in is null.");
            throw new IllegalArgumentException();
        }
        trainerStorage.getStorage().put(trainer.getId(), trainer);
        log.info("Saved the trainer successfully with ID: {}", trainer.getId());
        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Map<Long, Trainer> storage = trainerStorage.getStorage();
        if (!storage.containsKey(trainer.getId())) {
            log.error("Cannot update. Trainer with id {} not found", trainer.getId());
            throw new TrainerNotFoundException();
        }
        storage.put(trainer.getId(), trainer);
        log.info("Updated trainer with id {}", trainer.getId());
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Optional<Trainer> trainer = Optional.ofNullable(trainerStorage.getStorage().get(id));
        trainer.ifPresentOrElse(t -> log.info("Trainer found with id {}",id),
                () -> log.warn("Trainer not found with id {}", id));
        return trainer;
    }

    @Override
    public List<Trainer> findAll() {
        log.info("Attempting to collect every trainer");
        ArrayList<Trainer> all = new ArrayList<>(trainerStorage.getStorage().values());
        log.info("Successfully found {} trainers", all.size());
        return all;
    }

    @Override
    public void delete(long id) {
        trainerStorage.getStorage().remove(id);
        log.debug("Deleted trainer with id {}. Total trainers: {}", id, trainerStorage.getStorage().size());
    }
}