package com.gymcrm.dao.impl;

import com.gymcrm.dao.TrainerDao;
import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.model.Trainer;
import com.gymcrm.storage.TrainerStorage;
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
        if (trainer == null || trainer.getTrainerId() == null) {
            log.error("The trainer or the trainer id passed in is null.");
            throw new IllegalArgumentException();
        }
        trainerStorage.getStorage().put(trainer.getTrainerId(), trainer);
        log.info("Saved the trainer successfully with ID: {}", trainer.getTrainerId());
        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Map<Long, Trainer> storage = trainerStorage.getStorage();
        if (!storage.containsKey(trainer.getTrainerId())) {
            log.error("Cannot update. Trainer with id {} not found", trainer.getTrainerId());
            throw new TrainerNotFoundException();
        }
        storage.put(trainer.getTrainerId(), trainer);
        log.info("Updated trainer with id {}", trainer.getTrainerId());
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