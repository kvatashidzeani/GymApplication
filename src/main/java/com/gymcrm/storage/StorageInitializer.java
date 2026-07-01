package com.gymcrm.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymcrm.Loader.Loader;
import com.gymcrm.Loader.SeedDataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class StorageInitializer {

    private final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private SeedDataContext seedDataContext;
    private List<Loader> loaders;

    @Value("${data.storage}")
    private Resource dataFile;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    public void setSeedDataContext(SeedDataContext seedDataContext) {
        this.seedDataContext = seedDataContext;
    }

    @Autowired
    public void setLoaders(List<Loader> loaders) {
        this.loaders = loaders;
    }

    @PostConstruct
    public void init() {
        try (InputStream is = dataFile.getInputStream()) {
            SeedData data = mapper.readValue(is, SeedData.class);
            seedDataContext.setSeedData(data);

            log.info("Starting storage instantiation");

            loaders.stream()
                    .sorted(Comparator.comparingInt(Loader::getOrder))
                    .forEach(loader -> {
                        log.info("Executing loader: {}", loader.getClass().getSimpleName());
                        loader.load();
                        log.info("{} finished successfully", loader.getClass().getSimpleName());
                    });

            log.info("All storage objects successfully initialized.");
        } catch (Exception e) {
            log.error("Storage initialization failed", e);
            throw new IllegalStateException("Could not initialize storage", e);
        }
    }

    public static class SeedData {
        private List<String> trainingTypes;
        private List<TraineeSeed> trainees;
        private List<TrainerSeed> trainers;
        private List<TrainingSeed> trainings;

        public List<String> getTrainingTypes() { return trainingTypes; }
        public void setTrainingTypes(List<String> trainingTypes) { this.trainingTypes = trainingTypes; }

        public List<TraineeSeed> getTrainees() { return trainees; }
        public void setTrainees(List<TraineeSeed> trainees) { this.trainees = trainees; }

        public List<TrainerSeed> getTrainers() { return trainers; }
        public void setTrainers(List<TrainerSeed> trainers) { this.trainers = trainers; }

        public List<TrainingSeed> getTrainings() { return trainings; }
        public void setTrainings(List<TrainingSeed> trainings) { this.trainings = trainings; }
    }

    public static class TraineeSeed {
        private String firstName;
        private String lastName;
        private String address;
        private LocalDate dateOfBirth;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    }

    public static class TrainerSeed {
        private String firstName;
        private String lastName;
        private String specializationName;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getSpecializationName() { return specializationName; }
        public void setSpecializationName(String specializationName) { this.specializationName = specializationName; }
    }

    public static class TrainingSeed {
        private Long traineeId;
        private Long trainerId;
        private String trainingName;
        private String trainingTypeName;
        private LocalDate trainingDate;
        private Integer trainingDurationMinutes;

        public Long getTraineeId() { return traineeId; }
        public void setTraineeId(Long traineeId) { this.traineeId = traineeId; }

        public Long getTrainerId() { return trainerId; }
        public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

        public String getTrainingName() { return trainingName; }
        public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

        public String getTrainingTypeName() { return trainingTypeName; }
        public void setTrainingTypeName(String trainingTypeName) { this.trainingTypeName = trainingTypeName; }

        public LocalDate getTrainingDate() { return trainingDate; }
        public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

        public Integer getTrainingDurationMinutes() { return trainingDurationMinutes; }
        public void setTrainingDurationMinutes(Integer trainingDurationMinutes) { this.trainingDurationMinutes = trainingDurationMinutes; }
    }
}