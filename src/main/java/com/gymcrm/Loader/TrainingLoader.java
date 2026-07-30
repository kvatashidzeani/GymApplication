package com.gymcrm.Loader;

import com.gymcrm.dao.TraineeDao;
import com.gymcrm.dao.TrainerDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.TrainingService;
import com.gymcrm.storage.TrainingTypeStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TrainingLoader implements Loader {

    private TrainingService trainingService;
    private SeedDataContext context;
    private TrainingTypeStorage trainingTypeStorage;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private final Logger log = LoggerFactory.getLogger(TrainingLoader.class);

    @Autowired
    public void setTrainingService(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Autowired
    public void setContext(SeedDataContext context) {
        this.context = context;
    }

    @Autowired
    public void setTrainingTypeStorage(TrainingTypeStorage trainingTypeStorage) {
        this.trainingTypeStorage = trainingTypeStorage;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public void load() {
        var trainings = context.getSeedData().getTrainings();
        if (trainings == null || trainings.isEmpty()) {
            log.warn("No trainings to load.");
            return;
        }

        var typeMap = trainingTypeStorage.getAllByName();
        List<Trainee> trainees = traineeDao.findAll().stream()
                .sorted(Comparator.comparing(Trainee::getId))
                .toList();
        List<Trainer> trainers = trainerDao.findAll().stream()
                .sorted(Comparator.comparing(Trainer::getId))
                .toList();

        if (trainees.isEmpty() || trainers.isEmpty()) {
            log.error("Cannot seed trainings: trainees={}, trainers={}", trainees.size(), trainers.size());
            return;
        }

        for (int i = 0; i < trainings.size(); i++) {
            var t = trainings.get(i);
            TrainingType type = typeMap.get(t.getTrainingTypeName());
            if (type == null) {
                log.error("Unknown training type: {}", t.getTrainingTypeName());
                continue;
            }

            Long traineeId = resolveTraineeId(t.getTraineeId(), trainees, i);
            Long trainerId = resolveTrainerId(t.getTrainerId(), trainers, i);

            trainingService.createTraining(
                    traineeId,
                    trainerId,
                    t.getTrainingName(),
                    type,
                    t.getTrainingDate(),
                    t.getTrainingDurationMinutes()
            );

            // Keep M2M assignment in sync so trainer profile shows trainees
            traineeDao.findById(traineeId).ifPresent(trainee -> {
                trainee.getTrainerIds().add(trainerId);
                traineeDao.update(trainee);
            });

            log.info("Seeded training: {} (traineeId={}, trainerId={})", t.getTrainingName(), traineeId, trainerId);
        }
        log.info("Successfully parsed the trainings.");
    }

    private Long resolveTraineeId(Long seedTraineeId, List<Trainee> trainees, int index) {
        if (seedTraineeId != null && traineeDao.findById(seedTraineeId).isPresent()) {
            return seedTraineeId;
        }
        Long resolved = trainees.get(index % trainees.size()).getId();
        log.warn("Using trainee id {} instead of seed id {}", resolved, seedTraineeId);
        return resolved;
    }

    private Long resolveTrainerId(Long seedTrainerId, List<Trainer> trainers, int index) {
        if (seedTrainerId != null && trainerDao.findById(seedTrainerId).isPresent()) {
            return seedTrainerId;
        }
        Long resolved = trainers.get(index % trainers.size()).getId();
        log.warn("Using trainer id {} instead of seed id {}", resolved, seedTrainerId);
        return resolved;
    }
}
