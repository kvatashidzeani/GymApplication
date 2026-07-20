package com.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@Table(name = "\"user\"")
public class User {

    @Id
    @Column(name = "id")
    @JdbcTypeCode(Types.INTEGER)
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public User() {}

    public User(String firstName, String lastName, String username, String password, boolean isActive) {
        this(firstName, lastName, username, password, isActive, null);
    }

    public User(String firstName, String lastName, String username, String password,
                boolean isActive, Long userId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.isActive = isActive;
        this.userId = userId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "com.gymcrm.model.User{userId=" + userId + ", firstName='" + firstName +
                "', lastName='" + lastName + "', username='" + username + "', isActive=" + isActive + "}";
    }
}
