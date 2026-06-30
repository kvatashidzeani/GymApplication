package com.gym.crm.dao;


import com.gym.crm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    void delete(long id);
    Trainer save(Trainer trainer);
    Trainer update(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();
}