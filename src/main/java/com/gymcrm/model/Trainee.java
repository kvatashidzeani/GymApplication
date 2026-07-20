package com.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Matches DB schema: trainee(id, date_of_birth, address, user_id FK → user).
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

    @Column(name = "user_id", nullable = false, unique = true)
    @JdbcTypeCode(Types.INTEGER)
    private Long userId;

    @Transient
    private Set<Long> trainerIds = new HashSet<>();

    /** In-memory convenience link; persisted relation is user_id column. */
    @Transient
    private User user;

    public Trainee() {}

    public Trainee(Long id, LocalDate dateOfBirth, String address, Long userId) {
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Set<Long> getTrainerIds() { return trainerIds; }
    public void setTrainerIds(Set<Long> trainerIds) {
        this.trainerIds = trainerIds == null ? new HashSet<>() : new HashSet<>(trainerIds);
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Trainee{id=" + id + ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + "', userId=" + userId +
                ", trainerIds=" + trainerIds + ", user=" + user + "}";
    }
}
