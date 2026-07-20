package com.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

/**
 * Matches DB schema: trainer(id, specialization FK → training_type, user_id FK → user).
 */
@Entity
@Table(name = "trainer")
public class Trainer {

    @Id
    @JdbcTypeCode(Types.INTEGER)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialization", referencedColumnName = "id")
    private TrainingType specialization;

    @Column(name = "user_id", nullable = false, unique = true)
    @JdbcTypeCode(Types.INTEGER)
    private Long userId;

    /** In-memory convenience link; persisted relation is user_id column. */
    @Transient
    private User user;

    public Trainer() {}

    public Trainer(Long id, TrainingType specialization, Long userId) {
        this.id = id;
        this.specialization = specialization;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TrainingType getSpecialization() { return specialization; }
    public void setSpecialization(TrainingType specialization) { this.specialization = specialization; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Trainer{id=" + id + ", specialization=" + specialization +
                ", userId=" + userId + ", user=" + user + "}";
    }
}
