package com.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Child of User (one-to-one): trainee.user_id FK → user.id (unique).
 * trainerIds is kept in memory for M2M assignment (not in DB schema).
 */
@Entity
@Table(name = "trainee")
public class Trainee {

    @Id
    @JdbcTypeCode(Types.INTEGER)
    private Long id;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 255)
    private String address;

    /**
     * Owning side of User ↔ Trainee one-to-one.
     * Parent = User, child = Trainee (FK user_id).
     */
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Transient
    private Set<Long> trainerIds = new HashSet<>();

    public Trainee() {}

    public Trainee(Long id, LocalDate dateOfBirth, String address, Long userId) {
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        setUserId(userId);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public User getUser() { return user; }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            user.setTrainee(this);
        }
    }

    /** Convenience for FK id access used by services/DAOs. */
    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }

    public void setUserId(Long userId) {
        if (userId == null) {
            if (this.user != null) {
                this.user.setTrainee(null);
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

    public Set<Long> getTrainerIds() { return trainerIds; }
    public void setTrainerIds(Set<Long> trainerIds) {
        this.trainerIds = trainerIds == null ? new HashSet<>() : new HashSet<>(trainerIds);
    }

    @Override
    public String toString() {
        return "Trainee{id=" + id + ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + "', userId=" + getUserId() +
                ", trainerIds=" + trainerIds + ", user=" + user + "}";
    }
}
