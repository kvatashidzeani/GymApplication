package com.gymcrm.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Response for Get / Update Trainee Profile.
 */
public class TraineeProfileResponse {

    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private boolean isActive;
    private List<TrainerShortDto> trainersList = new ArrayList<>();

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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public List<TrainerShortDto> getTrainersList() {
        return trainersList;
    }

    public void setTrainersList(List<TrainerShortDto> trainersList) {
        this.trainersList = trainersList;
    }
}
