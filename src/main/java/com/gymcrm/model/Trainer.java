package com.gymcrm.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

/**
 * Child of User (one-to-one): trainer.user_id FK → user.id (unique).
 * Also references TrainingType via specialization.
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

    /**
     * Owning side of User ↔ Trainer one-to-one.
     * Parent = User, child = Trainer (FK user_id).
     */
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    public Trainer() {}

    public Trainer(Long id, TrainingType specialization, Long userId) {
        this.id = id;
        this.specialization = specialization;
        setUserId(userId);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TrainingType getSpecialization() { return specialization; }
    public void setSpecialization(TrainingType specialization) { this.specialization = specialization; }

    public User getUser() { return user; }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            user.setTrainer(this);
        }
    }

    /** Convenience for FK id access used by services/DAOs. */
    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }

    public void setUserId(Long userId) {
        if (userId == null) {
            if (this.user != null) {
                this.user.setTrainer(null);
            }
            this.user = null;
            return;
        }
        if (this.user != null && userId.equals(this.user.getUserId())) {
            return;
        }
        User stub = new User();
        stub.setUserId(userId);
        setUser(stub);
    }

    @Override
    public String toString() {
        return "Trainer{id=" + id + ", specialization=" + specialization +
                ", userId=" + getUserId() + ", user=" + user + "}";
    }
}
