package com.gymcrm.service;

import com.gymcrm.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training createTraining(Training training);
    Optional<Training> selectTraining(Long id);
    List<Training> selectAllTrainings();
}