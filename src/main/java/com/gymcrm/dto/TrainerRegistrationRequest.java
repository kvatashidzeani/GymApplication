package com.gymcrm.dto;

/**
 * Request body for Trainer Registration (POST).
 * specialization = training type name (e.g. "Cardio", "Yoga").
 */
public class TrainerRegistrationRequest {

    private String firstName;
    private String lastName;
    private String specialization;

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
}
