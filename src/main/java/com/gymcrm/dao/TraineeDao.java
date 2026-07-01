package com.gymcrm.dao;

import com.gymcrm.model.Trainee;
import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Trainee trainee);
    Trainee update(Trainee update);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
    void delete(Long id);
}