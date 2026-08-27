package com.gymcrm.workload.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * One calendar year with a list of monthly summaries.
 */
public class YearWorkload {

    private int year;
    private List<MonthWorkload> months = new ArrayList<>();

    public YearWorkload() {
    }

    public YearWorkload(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<MonthWorkload> getMonths() {
        return months;
    }

    public void setMonths(List<MonthWorkload> months) {
        this.months = months;
    }

    public Optional<MonthWorkload> findMonth(int month) {
        return months.stream().filter(m -> m.getMonth() == month).findFirst();
    }

    public MonthWorkload getOrCreateMonth(int month) {
        return findMonth(month).orElseGet(() -> {
            MonthWorkload created = new MonthWorkload(month, 0);
            months.add(created);
            months.sort((a, b) -> Integer.compare(a.getMonth(), b.getMonth()));
            return created;
        });
    }

    public void removeMonthIfEmpty(int month) {
        months.removeIf(m -> m.getMonth() == month && m.getTrainingSummaryDuration() <= 0);
    }
}
