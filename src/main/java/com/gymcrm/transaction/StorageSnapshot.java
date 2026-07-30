package com.gymcrm.transaction;

import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.User;
import com.gymcrm.storage.TraineeStorage;
import com.gymcrm.storage.TrainerStorage;
import com.gymcrm.storage.TrainingStorage;
import com.gymcrm.storage.UserStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Point-in-time deep copy of in-memory storage maps for transaction rollback.
 */
final class StorageSnapshot {

    private final Map<Long, User> users;
    private final Map<Long, Trainee> trainees;
    private final Map<Long, Trainer> trainers;
    private final Map<Long, Training> trainings;

    private StorageSnapshot(Map<Long, User> users,
                            Map<Long, Trainee> trainees,
                            Map<Long, Trainer> trainers,
                            Map<Long, Training> trainings) {
        this.users = users;
        this.trainees = trainees;
        this.trainers = trainers;
        this.trainings = trainings;
    }

    static StorageSnapshot capture(UserStorage userStorage,
                                   TraineeStorage traineeStorage,
                                   TrainerStorage trainerStorage,
                                   TrainingStorage trainingStorage) {
        Map<Long, User> users = new HashMap<>();
        userStorage.getStorage().forEach((id, user) -> users.put(id, copyUser(user)));

        Map<Long, Trainee> trainees = new HashMap<>();
        traineeStorage.getStorage().forEach((id, trainee) -> trainees.put(id, copyTrainee(trainee)));

        Map<Long, Trainer> trainers = new HashMap<>();
        trainerStorage.getStorage().forEach((id, trainer) -> trainers.put(id, copyTrainer(trainer)));

        Map<Long, Training> trainings = new HashMap<>();
        trainingStorage.getStorage().forEach((id, training) -> trainings.put(id, copyTraining(training)));

        return new StorageSnapshot(users, trainees, trainers, trainings);
    }

    static void restore(StorageSnapshot snapshot,
                        UserStorage userStorage,
                        TraineeStorage traineeStorage,
                        TrainerStorage trainerStorage,
                        TrainingStorage trainingStorage) {
        replaceMap(userStorage.getStorage(), snapshot.users);
        replaceMap(traineeStorage.getStorage(), snapshot.trainees);
        replaceMap(trainerStorage.getStorage(), snapshot.trainers);
        replaceMap(trainingStorage.getStorage(), snapshot.trainings);
    }

    private static <K, V> void replaceMap(Map<K, V> target, Map<K, V> source) {
        target.clear();
        target.putAll(source);
    }

    private static User copyUser(User user) {
        if (user == null) {
            return null;
        }
        User copy = new User();
        copy.setUserId(user.getUserId());
        copy.setFirstName(user.getFirstName());
        copy.setLastName(user.getLastName());
        copy.setUsername(user.getUsername());
        copy.setPassword(user.getPassword());
        copy.setActive(user.isActive());
        return copy;
    }

    private static Trainee copyTrainee(Trainee trainee) {
        if (trainee == null) {
            return null;
        }
        Trainee copy = new Trainee();
        copy.setId(trainee.getId());
        copy.setDateOfBirth(trainee.getDateOfBirth());
        copy.setAddress(trainee.getAddress());
        copy.setTrainerIds(new HashSet<>(trainee.getTrainerIds()));
        copy.setUser(copyUser(trainee.getUser()));
        return copy;
    }

    private static Trainer copyTrainer(Trainer trainer) {
        if (trainer == null) {
            return null;
        }
        Trainer copy = new Trainer();
        copy.setId(trainer.getId());
        copy.setSpecialization(trainer.getSpecialization());
        copy.setUser(copyUser(trainer.getUser()));
        return copy;
    }

    private static Training copyTraining(Training training) {
        if (training == null) {
            return null;
        }
        Training copy = new Training();
        copy.setId(training.getId());
        copy.setTraineeId(training.getTraineeId());
        copy.setTrainerId(training.getTrainerId());
        copy.setTrainingName(training.getTrainingName());
        copy.setTrainingType(training.getTrainingType());
        copy.setTrainingDate(training.getTrainingDate());
        copy.setTrainingDuration(training.getTrainingDuration());
        return copy;
    }
}
