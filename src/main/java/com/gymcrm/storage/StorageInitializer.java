package com.gymcrm.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryStorage {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryStorage.class);

    private final Map<Long, Trainer> trainerStorage = new HashMap<>();
    private final Map<Long, Trainee> traineeStorage = new HashMap<>();
    private final Map<Long, Training> trainingStorage = new HashMap<>();

    @Value("${storage.init.file}")
    private String initFilePath;

    @PostConstruct
    public void initializeStorage() {
        logger.info("Initializing in-memory storage from file: {}", initFilePath);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            InputStream is = getClass().getClassLoader().getResourceAsStream(initFilePath);
            if (is == null) {
                logger.warn("Initialization file not found: {}. Storage will start empty.", initFilePath);
                return;
            }

            JsonNode root = mapper.readTree(is);

            JsonNode trainers = root.path("trainers");
            for (JsonNode node : trainers) {
                Trainer trainer = new Trainer(
                        node.path("firstName").asText(),
                        node.path("lastName").asText(),
                        node.path("username").asText(),
                        node.path("password").asText(),
                        node.path("isActive").asBoolean(true),
                        node.path("userId").asLong(),
                        node.path("specialization").asText()
                );
                trainerStorage.put(trainer.getUserId(), trainer);
                logger.debug("Loaded trainer: {}", trainer.getUsername());
            }

            JsonNode trainees = root.path("trainees");
            for (JsonNode node : trainees) {
                String dobStr = node.path("dateOfBirth").asText(null);
                LocalDate dob = (dobStr != null && !dobStr.isEmpty()) ? LocalDate.parse(dobStr) : null;
                Trainee trainee = new Trainee(
                        node.path("firstName").asText(),
                        node.path("lastName").asText(),
                        node.path("username").asText(),
                        node.path("password").asText(),
                        node.path("isActive").asBoolean(true),
                        node.path("userId").asLong(),
                        dob,
                        node.path("address").asText()
                );
                traineeStorage.put(trainee.getUserId(), trainee);
                logger.debug("Loaded trainee: {}", trainee.getUsername());
            }

            JsonNode trainings = root.path("trainings");
            for (JsonNode node : trainings) {
                TrainingType type = new TrainingType(node.path("trainingTypeName").asText());
                Training training = new Training(
                        node.path("id").asLong(),
                        node.path("traineeId").asLong(),
                        node.path("trainerId").asLong(),
                        node.path("trainingName").asText(),
                        type,
                        LocalDate.parse(node.path("trainingDate").asText()),
                        node.path("trainingDuration").asInt()
                );
                trainingStorage.put(training.getId(), training);
                logger.debug("Loaded training: {}", training.getTrainingName());
            }

            logger.info("Storage initialized: {} trainers, {} trainees, {} trainings loaded.",
                    trainerStorage.size(), traineeStorage.size(), trainingStorage.size());

        } catch (Exception e) {
            logger.error("Failed to initialize storage from file '{}': {}", initFilePath, e.getMessage(), e);
        }
    }

    public Map<Long, Trainer> getTrainerStorage() { return trainerStorage; }
    public Map<Long, Trainee> getTraineeStorage() { return traineeStorage; }
    public Map<Long, Training> getTrainingStorage() { return trainingStorage; }
}