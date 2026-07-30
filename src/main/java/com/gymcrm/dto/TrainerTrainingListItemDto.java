package com.gymcrm.dto;

import java.time.LocalDate;

/**
 * Training summary for Get Trainer Trainings List response.
 */
public class TrainerTrainingListItemDto {

    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private int trainingDuration;
    private String traineeName;

    public TrainerTrainingListItemDto() {
    }

    public TrainerTrainingListItemDto(String trainingName,
                                      LocalDate trainingDate,
                                      String trainingType,
                                      int trainingDuration,
                                      String traineeName) {
        this.trainingName = trainingName;
        this.trainingDate = trainingDate;
        this.trainingType = trainingType;
        this.trainingDuration = trainingDuration;
        this.traineeName = traineeName;
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

    public String getTraineeName() {
        return traineeName;
    }

    public void setTraineeName(String traineeName) {
        this.traineeName = traineeName;
    }
}
