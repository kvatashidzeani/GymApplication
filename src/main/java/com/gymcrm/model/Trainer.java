package com.gymcrm.model;

public class Trainer extends User {

    private Long trainerId;
    private TrainingType specialization;

    public Trainer() {}

    public Trainer(String firstName, String lastName, String username, String password,
                   boolean isActive, Long userId, TrainingType specialization) {
        super(firstName, lastName, username, password, isActive);
        this.trainerId = userId;
        this.specialization = specialization;
    }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long userId) { this.trainerId = userId; }

    public TrainingType getSpecialization() { return specialization; }
    public void setSpecialization(TrainingType specialization) { this.specialization = specialization; }

    @Override
    public String toString() {
        return "Trainer{userId=" + trainerId + ", specialization='" + specialization +
                "', " + super.toString() + "}";
    }
}