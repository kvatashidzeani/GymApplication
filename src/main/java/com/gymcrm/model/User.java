package com.gymcrm.model;
<<<<<<< Updated upstream
=======

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

/**
 * Parent table for Trainee and Trainer (one-to-one).
 * A User may be linked to at most one Trainee or one Trainer.
 */
@Entity
@Table(name = "\"user\"")
>>>>>>> Stashed changes
public class User {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
<<<<<<< Updated upstream
    private boolean isActive;
=======

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * One-time plain password exposed only right after registration for the API response.
     * Never persisted — storage always keeps the BCrypt hash in {@link #password}.
     */
    @Transient
    private String rawPassword;

    /** Inverse side of Trainee → User (child owns FK user_id). */
    @OneToOne(mappedBy = "user")
    private Trainee trainee;

    /** Inverse side of Trainer → User (child owns FK user_id). */
    @OneToOne(mappedBy = "user")
    private Trainer trainer;
>>>>>>> Stashed changes

    public User() {}

    public User(String firstName, String lastName, String username, String password, boolean isActive) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.isActive = isActive;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRawPassword() { return rawPassword; }
    public void setRawPassword(String rawPassword) { this.rawPassword = rawPassword; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "com.gymcrm.model.User{firstName='" + firstName + "', lastName='" + lastName +
                "', username='" + username + "', isActive=" + isActive + "}";
    }
}