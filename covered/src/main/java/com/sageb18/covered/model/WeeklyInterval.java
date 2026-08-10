package com.sageb18.covered.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * A block of time on an abstract repeating week (e.g. MONDAY 08:00-14:00).
 * Implemented by Shift and UnavailabilityWindow so the overlap rule lives in one place.
 */
public interface WeeklyInterval {

    int MINUTES_PER_DAY = 24 * 60;

    DayOfWeek getDayOfWeek();

    LocalTime getStart();

    LocalTime getEnd();

    default int getStartMinuteOfWeek() {
        return toMinuteOfWeek(getDayOfWeek(), getStart());
    }

    default int getEndMinuteOfWeek() {
        return toMinuteOfWeek(getDayOfWeek(), getEnd());
    }

    default int getDurationMinutes() {
        return getEndMinuteOfWeek() - getStartMinuteOfWeek();
    }

    /** half-open: a shift ending at 14:00 does not overlap one starting at 14:00 */
    default boolean overlaps(WeeklyInterval other) {
        return getStartMinuteOfWeek() < other.getEndMinuteOfWeek()
                && other.getStartMinuteOfWeek() < getEndMinuteOfWeek();
    }

    /** minutes since Monday 00:00, 0..10079 */
    static int toMinuteOfWeek(DayOfWeek dayOfWeek, LocalTime time) {
        return (dayOfWeek.getValue() - 1) * MINUTES_PER_DAY + (time.toSecondOfDay() / 60);
    }
}
