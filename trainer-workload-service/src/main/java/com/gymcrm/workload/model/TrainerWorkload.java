package com.gymcrm.workload.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory trainer workload aggregate:
 * Username, First Name, Last Name, Status, Years → Months → duration.
 */
public class TrainerWorkload {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean trainerStatus;
    private List<YearWorkload> years = new ArrayList<>();

    public TrainerWorkload() {
    }

    public TrainerWorkload(String trainerUsername, String trainerFirstName,
                           String trainerLastName, boolean trainerStatus) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.trainerStatus = trainerStatus;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public boolean isTrainerStatus() {
        return trainerStatus;
    }

    public void setTrainerStatus(boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
    }

    public List<YearWorkload> getYears() {
        return years;
    }

    public void setYears(List<YearWorkload> years) {
        this.years = years;
    }

    public Optional<YearWorkload> findYear(int year) {
        return years.stream().filter(y -> y.getYear() == year).findFirst();
    }

    public YearWorkload getOrCreateYear(int year) {
        return findYear(year).orElseGet(() -> {
            YearWorkload created = new YearWorkload(year);
            years.add(created);
            years.sort((a, b) -> Integer.compare(a.getYear(), b.getYear()));
            return created;
        });
    }

    public void removeYearIfEmpty(int year) {
        years.removeIf(y -> y.getYear() == year && y.getMonths().isEmpty());
    }
}
