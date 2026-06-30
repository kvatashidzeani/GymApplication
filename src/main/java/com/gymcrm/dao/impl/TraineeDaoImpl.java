package com.gym.crm.dao.impl;

import com.gym.crm.exceptions.TraineeNotFoundException;
import com.gym.crm.dao.TraineeDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.storage.TraineeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {
    private TraineeStorage traineeStorage;
    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);


    
    @Autowired
    public void setTraineeStorage(TraineeStorage traineeStorage){
        this.traineeStorage = traineeStorage;
        }

    @Override
    public Trainee save(Trainee trainee) {
        if (trainee == null || trainee.getId() == null) {
            throw new IllegalArgumentException("Trainee or ID cannot be null");
        }
        traineeStorage.getStorage().put(trainee.getId(), trainee);
        log.debug("Trainee saved with id {}. Total trainees: {}", trainee.getId(), traineeStorage.getStorage().size());
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (trainee == null || trainee.getId() == null) {
            throw new IllegalArgumentException("Trainee or trainee ID is null");
        }
        Long id = trainee.getId();
        log.info("Updating the trainee with the id {}", id);
        if (!traineeStorage.getStorage().containsKey(id)) {
            log.warn("The trainee with the id {} does not exist", id);
            throw new TraineeNotFoundException();
        }
        traineeStorage.getStorage().put(id, trainee);
        log.info("Successfully updated the trainee with the id {}", id);
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Optional<Trainee> trainee = Optional.ofNullable(traineeStorage.getStorage().get(id));
        trainee.ifPresentOrElse(t -> log.info("Trainee found with id {}",id),
                () -> log.warn("Trainee NOT found with id {}", id));

        return trainee;
    }

    @Override
    public List<Trainee> findAll() {
        log.info("Attempting to collect every trainee");
        ArrayList<Trainee> all = new ArrayList<>(traineeStorage.getStorage().values());
        log.info("Successfully found {} trainees", all.size());
        return all;
    }

    @Override
    public void delete(Long id) {
        traineeStorage.getStorage().remove(id);
        log.info("Successfully deleted the trainee with the id {}", id);
    }
}