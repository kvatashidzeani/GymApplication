package com.gymcrm.workload.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB document: trainer training summary.
 * <p>
 * Collection {@code trainer_training_summary} with nested years → months → duration.
 */
@Document(collection = "trainer_training_summary")
@CompoundIndex(name = "trainer_name_idx", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
public class TrainerWorkload {

    @Id
    @NotBlank
    @Field("trainerUsername")
    private String trainerUsername;

    @NotBlank
    @Field("trainerFirstName")
    private String trainerFirstName;

    @NotBlank
    @Field("trainerLastName")
    private String trainerLastName;

    @NotNull
    @Field("trainerStatus")
    private Boolean trainerStatus;

    @NotNull
    @Field("years")
    private List<YearWorkload> years = new ArrayList<>();

    public TrainerWorkload() {
    }

    public TrainerWorkload(String trainerUsername, String trainerFirstName,
                           String trainerLastName, Boolean trainerStatus) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.trainerStatus = trainerStatus;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public Boolean getTrainerStatus() {
        return trainerStatus;
    }

    public void setTrainerStatus(Boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
    }

    public List<YearWorkload> getYears() {
        return years;
    }

    public void setYears(List<YearWorkload> years) {
        this.years = years;
    }

    public Optional<YearWorkload> findYear(int year) {
        return years.stream().filter(y -> y.getYear() == year).findFirst();
    }

    public YearWorkload getOrCreateYear(int year) {
        return findYear(year).orElseGet(() -> {
            YearWorkload created = new YearWorkload(year);
            years.add(created);
            years.sort((a, b) -> Integer.compare(a.getYear(), b.getYear()));
            return created;
        });
    }

    public void removeYearIfEmpty(int year) {
        years.removeIf(y -> y.getYear() == year && y.getMonths().isEmpty());
    }
}
