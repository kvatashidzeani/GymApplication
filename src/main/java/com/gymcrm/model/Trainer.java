package com.gymcrm.model;
public class Trainer extends User {

    private Long userId;
    private String specialization;

    public Trainer() {}

    public Trainer(String firstName, String lastName, String username, String password,
                   boolean isActive, Long userId, String specialization) {
        super(firstName, lastName, username, password, isActive);
        this.userId = userId;
        this.specialization = specialization;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    @Override
    public String toString() {
        return "Trainer{userId=" + userId + ", specialization='" + specialization +
                "', " + super.toString() + "}";
    }
}