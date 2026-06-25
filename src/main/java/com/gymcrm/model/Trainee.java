package com.gymcrm.model;

import java.time.LocalDate;

public class Trainee extends User {

    private Long userId;
    private LocalDate dateOfBirth;
    private String address;

    public Trainee() {}

    public Trainee(String firstName, String lastName, String username, String password,
                   boolean isActive, Long userId, LocalDate dateOfBirth, String address) {
        super(firstName, lastName, username, password, isActive);
        this.userId = userId;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return "com.gymcrm.model.Trainee{userId=" + userId + ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + "', " + super.toString() + "}";
    }
}