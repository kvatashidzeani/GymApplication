package com.gymcrm.dao;

import com.gymcrm.model.Training;import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    Training save(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();
}