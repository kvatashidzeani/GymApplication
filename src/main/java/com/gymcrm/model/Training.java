package com.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;

@Entity
@Table(name = "training")
public class Training {

    @Id
    @JdbcTypeCode(Types.INTEGER)
    private Long id;

    @Column(name = "trainee_id", nullable = false)
    @JdbcTypeCode(Types.INTEGER)
    private Long traineeId;

    @Column(name = "trainer_id", nullable = false)
    @JdbcTypeCode(Types.INTEGER)
    private Long trainerId;

    @Column(name = "training_name", nullable = false, length = 150)
    private String trainingName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "training_type_id", referencedColumnName = "id")
    private TrainingType trainingType;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    @JdbcTypeCode(Types.NUMERIC)
    private int trainingDuration;

    public Training() {}

    public Training(Long id, Long traineeId, Long trainerId, String trainingName,
                    TrainingType trainingType, LocalDate trainingDate, int trainingDuration) {
        this.id = id;
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTraineeId() { return traineeId; }
    public void setTraineeId(Long traineeId) { this.traineeId = traineeId; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public TrainingType getTrainingType() { return trainingType; }
    public void setTrainingType(TrainingType trainingType) { this.trainingType = trainingType; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public int getTrainingDuration() { return trainingDuration; }
    public void setTrainingDuration(int trainingDuration) { this.trainingDuration = trainingDuration; }

    @Override
    public String toString() {
        return "Training{id=" + id + ", traineeId=" + traineeId + ", trainerId=" + trainerId +
                ", trainingName='" + trainingName + "', trainingType=" + trainingType +
                ", trainingDate=" + trainingDate + ", trainingDuration=" + trainingDuration + "}";
    }
}
