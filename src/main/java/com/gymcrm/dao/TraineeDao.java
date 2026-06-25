package com.gymcrm.dao;

import com.gymcrm.model.Trainee;
import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee save(Trainee trainee);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
    Trainee update(Trainee trainee);
    void deleteById(Long id);
    boolean existsByUsername(String username);
    long countByFirstAndLastName(String firstName, String lastName);
}