package com.gymcrm.workload.model;

/**
 * One month's training summary duration (minutes).
 */
public class MonthWorkload {

    private int month;
    private int trainingSummaryDuration;

    public MonthWorkload() {
    }

    public MonthWorkload(int month, int trainingSummaryDuration) {
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getTrainingSummaryDuration() {
        return trainingSummaryDuration;
    }

    public void setTrainingSummaryDuration(int trainingSummaryDuration) {
        this.trainingSummaryDuration = trainingSummaryDuration;
    }
}
