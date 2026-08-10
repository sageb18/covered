package com.sageb18.covered.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record EmployeeDto(
        @NotNull(message = "employee id is required") UUID id,
        @NotBlank(message = "employee name is required") String name,
        @Positive(message = "maxHours must be greater than 0") int maxHours,
        Set<String> skills,
        @Valid List<UnavailabilityDto> unavailability) {

    public EmployeeDto {
        skills = (skills == null) ? Set.of() : Set.copyOf(skills);
        unavailability = (unavailability == null) ? List.of() : List.copyOf(unavailability);
    }
}
