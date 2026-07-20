package com.gymcrm.model;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Types;

/**
 * Constant training type from seed data. Immutable after creation.
 */
@Entity
@Table(name = "training_type")
@Immutable
public class TrainingType {

    @Id
    @Column(name = "id")
    @JdbcTypeCode(Types.INTEGER)
    private Long trainingTypeId;

    @Column(name = "training_type_name", nullable = false, unique = true, length = 100)
    private String trainingTypeName;

    protected TrainingType() {}

    public TrainingType(String trainingTypeName, Long trainingTypeId) {
        this.trainingTypeName = trainingTypeName;
        this.trainingTypeId = trainingTypeId;
    }

    public String getTrainingTypeName() { return trainingTypeName; }
    public Long getTrainingTypeId() { return trainingTypeId; }

    @Override
    public String toString() {
        return "TrainingType{trainingTypeName='" + trainingTypeName + "', trainingTypeId=" + trainingTypeId + "}";
    }
}
