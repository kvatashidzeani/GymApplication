package com.gymcrm.service;

import com.gymcrm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer createTrainer(Trainer trainer);
    Trainer updateTrainer(Trainer trainer);
    Optional<Trainer> selectTrainer(Long id);
    List<Trainer> selectAllTrainers();
}