package com.gymcrm.dao;
import com.gymcrm.model.Trainer;
import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer save(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();
    Trainer update(Trainer trainer);
    boolean existsByUsername(String username);
    long countByFirstAndLastName(String firstName, String lastName);
}