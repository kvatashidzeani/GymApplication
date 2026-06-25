package com.gymcrm.service;

import com.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee createTrainee(Trainee trainee);
    Trainee updateTrainee(Trainee trainee);
    void deleteTrainee(Long id);
    Optional<Trainee> selectTrainee(Long id);
    List<Trainee> selectAllTrainees();
}