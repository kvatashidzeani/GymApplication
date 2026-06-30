package com.gym.crm.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.Loader.Loader;
import com.gym.crm.Loader.SeedDataContext;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

@Component
public class StorageInitializer {

    private final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private  SeedDataContext seedDataContext;
    private  List<Loader> loaders;

    @Value("${data.storage}")
    private Resource dataFile;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    public void setSeedDataContext(SeedDataContext seedDataContext){
        this.seedDataContext = seedDataContext;
    }

    @Autowired
    public void setLoaders(List<Loader> loaders){
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


    @Getter
    @Setter
    public static class SeedData {
        private List<String> trainingTypes;
        private List<TraineeSeed> trainees;
        private List<TrainerSeed> trainers;
        private List<TrainingSeed> trainings;
    }

    @Getter
    @Setter
    public static class TraineeSeed {
        private String firstName;
        private String lastName;
        private String address;
        private java.time.LocalDate dateOfBirth;
    }

    @Getter
    @Setter
    public static class TrainerSeed {
        private String firstName;
        private String lastName;
        private String specializationName;
    }

    @Getter
    @Setter
    public static class TrainingSeed {
        private Long traineeId;
        private Long trainerId;
        private String trainingName;
        private String trainingTypeName;
        private java.time.LocalDate trainingDate;
        private Integer trainingDurationMinutes;
    }
}