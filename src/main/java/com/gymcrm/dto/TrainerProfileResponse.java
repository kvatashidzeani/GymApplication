package com.gymcrm.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response for Get / Update Trainer Profile.
 */
public class TrainerProfileResponse {

    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private boolean isActive;
    private List<TraineeShortDto> traineesList = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public List<TraineeShortDto> getTraineesList() {
        return traineesList;
    }

    public void setTraineesList(List<TraineeShortDto> traineesList) {
        this.traineesList = traineesList;
    }
}
