package com.gymcrm.model;

public class User {

    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean isActive;

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
