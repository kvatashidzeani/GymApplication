package com.gymcrm.workload.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response / view of trainer workload structure stored in MongoDB.
 */
public class TrainerWorkloadResponse {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean trainerStatus;
    private List<YearWorkloadDto> years = new ArrayList<>();

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

    public Boolean getTrainerStatus() {
        return trainerStatus;
    }

    public void setTrainerStatus(Boolean trainerStatus) {
        this.trainerStatus = trainerStatus;
    }

    public List<YearWorkloadDto> getYears() {
        return years;
    }

    public void setYears(List<YearWorkloadDto> years) {
        this.years = years;
    }

    public static class YearWorkloadDto {
        private int year;
        private List<MonthWorkloadDto> months = new ArrayList<>();

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public List<MonthWorkloadDto> getMonths() {
            return months;
        }

        public void setMonths(List<MonthWorkloadDto> months) {
            this.months = months;
        }
    }

    public static class MonthWorkloadDto {
        private int month;
        private Integer trainingSummaryDuration;

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public Integer getTrainingSummaryDuration() {
            return trainingSummaryDuration;
        }

        public void setTrainingSummaryDuration(Integer trainingSummaryDuration) {
            this.trainingSummaryDuration = trainingSummaryDuration;
        }
    }
}
