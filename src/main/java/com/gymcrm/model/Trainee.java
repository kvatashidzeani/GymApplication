package com.gymcrm.model;

import java.time.LocalDate;

/**
 * Matches DB schema: Trainee(id, dateOfBirth, address, userId FK → User).
 * Does not extend User — composition via userId.
 */
public class Trainee {

    private Long id;
    private LocalDate dateOfBirth;
    private String address;
    private Long userId;

    /** Convenience association (not a separate DB column). */
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

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "Trainee{id=" + id + ", dateOfBirth=" + dateOfBirth +
                ", address='" + address + "', userId=" + userId + ", user=" + user + "}";
    }
}
