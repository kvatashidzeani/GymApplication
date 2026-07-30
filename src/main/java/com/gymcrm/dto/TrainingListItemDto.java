package com.gymcrm.dto;

import java.time.LocalDate;

/**
 * Training summary for Get Trainee / Trainer Trainings List responses.
 */
public class TrainingListItemDto {

    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private int trainingDuration;
    private String trainerName;

    public TrainingListItemDto() {
    }

    public TrainingListItemDto(String trainingName,
                               LocalDate trainingDate,
                               String trainingType,
                               int trainingDuration,
                               String trainerName) {
        this.trainingName = trainingName;
        this.trainingDate = trainingDate;
        this.trainingType = trainingType;
        this.trainingDuration = trainingDuration;
        this.trainerName = trainerName;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public String getTrainingType() {
        return trainingType;
    }

    public void setTrainingType(String trainingType) {
        this.trainingType = trainingType;
    }

    public int getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(int trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }
}
