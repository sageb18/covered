package com.sageb18.covered.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record UnavailabilityDto(
        @NotNull(message = "dayOfWeek is required") DayOfWeek dayOfWeek,
        @NotNull(message = "start is required")
        @JsonFormat(pattern = ShiftDto.TIME_FORMAT) LocalTime start,

        @NotNull(message = "end is required")
        @JsonFormat(pattern = ShiftDto.TIME_FORMAT) LocalTime end) {

    /**
     * WeeklyInterval.getDurationMinutes() goes negative if end precedes start, which would
     * silently corrupt the max-hours constraint. Reject it at the boundary instead.
     * Bean Validation treats an is-prefixed boolean method as a property to check.
     */
    @JsonIgnore
    @AssertTrue(message = "unavailability end must be after start (windows cannot cross midnight)")
    public boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }
}
