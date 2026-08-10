package com.sageb18.covered.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SolveRequest(
        @NotEmpty(message = "at least one employee is required")
        @Valid List<EmployeeDto> employees,

        @NotEmpty(message = "at least one shift is required")
        @Valid List<ShiftDto> shifts) {

    public SolveRequest {
        employees = (employees == null) ? List.of() : List.copyOf(employees);
        shifts = (shifts == null) ? List.of() : List.copyOf(shifts);
    }
}
