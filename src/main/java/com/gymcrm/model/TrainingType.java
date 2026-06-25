package com.gymcrm.model;

public class TrainingType {

    private String trainingTypeName;

    public TrainingType() {}

    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public String getTrainingTypeName() { return trainingTypeName; }
    public void setTrainingTypeName(String trainingTypeName) { this.trainingTypeName = trainingTypeName; }

    @Override
    public String toString() {
        return "com.gymcrm.model.TrainingType{trainingTypeName='" + trainingTypeName + "'}";
    }
}