package com.gymcrm.model;

/**
 * Matches DB schema: Trainer(id, specialization FK → TrainingType, userId FK → User).
 * Does not extend User — composition via userId.
 */
public class Trainer {

    private Long id;
    private TrainingType specialization;
    private Long userId;

    /** Convenience association (not a separate DB column). */
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
