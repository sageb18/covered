package com.sageb18.covered.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record ShiftDto(
        @NotNull(message = "shift id is required") UUID id,
        @NotNull(message = "dayOfWeek is required") DayOfWeek dayOfWeek,
        @NotNull(message = "start is required")
        @JsonFormat(pattern = TIME_FORMAT) LocalTime start,

        @NotNull(message = "end is required")
        @JsonFormat(pattern = TIME_FORMAT) LocalTime end,
        @NotBlank(message = "requiredSkill is required") String requiredSkill) {

    static final String TIME_FORMAT = "HH:mm";

    @JsonIgnore
    @AssertTrue(message = "shift end must be after start (shifts cannot cross midnight)")
    public boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }
}
