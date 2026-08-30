package com.gymcrm.workload.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Embedded month document: training summary duration (minutes) for one month.
 */
public class MonthWorkload {

    @NotNull
    @Min(1)
    @Max(12)
    @Field("month")
    private Integer month;

    @NotNull
    @Min(0)
    @Field("trainingSummaryDuration")
    private Integer trainingSummaryDuration;

    public MonthWorkload() {
    }

    public MonthWorkload(int month, int trainingSummaryDuration) {
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
    }

    public MonthWorkload(Integer month, Integer trainingSummaryDuration) {
        this.month = month;
        this.trainingSummaryDuration = trainingSummaryDuration;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getTrainingSummaryDuration() {
        return trainingSummaryDuration;
    }

    public void setTrainingSummaryDuration(Integer trainingSummaryDuration) {
        this.trainingSummaryDuration = trainingSummaryDuration;
    }
}
