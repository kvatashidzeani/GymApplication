package com.gymcrm.model;

public class TrainingType {

    private String trainingTypeName;
    private Long trainingTypeId;

    public TrainingType() {}

    public TrainingType(String trainingTypeName,Long trainingTypeId) {
        this.trainingTypeName = trainingTypeName;
        this.trainingTypeId = trainingTypeId;
    }

    public String getTrainingTypeName() { return trainingTypeName; }
    public Long getTrainingTypeId() { return trainingTypeId; }
    public void setTrainingTypeName(String trainingTypeName) { this.trainingTypeName = trainingTypeName; }
    public void setTrainingTypeId(Long trainingTypeId) { this.trainingTypeId = trainingTypeId; }
    @Override
    public String toString() {
        return "com.gymcrm.model.TrainingType{trainingTypeName='" + trainingTypeName + "'}";
    }
}