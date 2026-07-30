package com.gymcrm.dto;

/**
 * Trainee summary used inside trainer profile responses.
 */
public class TraineeShortDto {

    private String username;
    private String firstName;
    private String lastName;

    public TraineeShortDto() {
    }

    public TraineeShortDto(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

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
}
