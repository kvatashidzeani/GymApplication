package com.gymcrm.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Payload matching trainer-workload-service {@code POST /workload}.
 */
public class WorkloadUpdateRequest {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;

    @JsonProperty("isActive")
    private Boolean isActive;

    private LocalDate trainingDate;
    private Integer trainingDuration;
    private WorkloadActionType actionType;

    public WorkloadUpdateRequest() {
    }

    public WorkloadUpdateRequest(String trainerUsername,
                                 String trainerFirstName,
                                 String trainerLastName,
                                 Boolean isActive,
                                 LocalDate trainingDate,
                                 Integer trainingDuration,
                                 WorkloadActionType actionType) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.isActive = isActive;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        this.actionType = actionType;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public WorkloadActionType getActionType() {
        return actionType;
    }

    public void setActionType(WorkloadActionType actionType) {
        this.actionType = actionType;
    }
}
